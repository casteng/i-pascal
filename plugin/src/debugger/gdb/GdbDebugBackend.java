package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.debugger.CommandSender;
import com.siberika.idea.pascal.debugger.DebugBackend;
import com.siberika.idea.pascal.debugger.DebugUtil;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;

import java.util.List;

public class GdbDebugBackend extends DebugBackend {

    private static final Logger LOG = Logger.getInstance(GdbDebugBackend.class);

    public GdbDebugBackend(PascalXDebugProcess process) {
        super(process);
    }

    @Override
    public void init() {
        options.supportsBulkDelete = false;
        options.useFullnameForBreakpoints = getData().getBoolean(PascalSdkData.Keys.DEBUGGER_BREAK_FULL_NAME);
        process.sendCommand("set print repeats unlimited");
        process.sendCommand("-break-delete");
    }

    @Override
    public void applySettings() {
        process.sendCommand("set print elements " + options.view.limitElements);
        process.sendCommand("set max-value-size " + options.view.limitValueSize);
    }

    @Override
    public void onSessionInit() {
        if (getData().getBoolean(PascalSdkData.Keys.DEBUGGER_REDIRECT_CONSOLE)) {
            if (SystemInfo.isWindows) {
                process.sendCommand("-gdb-set new-console on");
            } else {
                process.sendCommand("-exec-arguments > " + process.getOutputFile().getAbsolutePath());
            }
        }
        process.sendCommand("-exec-run");
        initPointerSize();
    }

    @Override
    public void createVar(String key, String expression, CommandSender.FinishCallback finishCallback) {
        process.sendCommand(String.format("-var-create \"%s\" @ \"%s\"", key, expression.replace("\"", "\\\"")), finishCallback);
    }

    @Override
    public void queryArrayValue(GdbVariableObject var, int start, long end) {
        String deref = var.getType().contains("(*)") ? "*" : "";
        evaluate(String.format("sizeof(%s%s[0])", deref, var.getName()), res -> {
            Integer elSize = DebugUtil.retrieveResultValueInt(res);
            if (null == elSize) {
                var.setError(PascalBundle.message("debug.expression.array.size.error"));
                return;
            }
            if (elSize > 1) {                           // Normal array data output
                evaluate(String.format("*&(%s%s)[%d]@%d", deref, var.getName(), start, end - start), res1 -> {
                    if (res1.getType() == GdbMiLine.Type.RESULT_RECORD && "done".equals(res1.getRecClass())) {
                        String valueRaw = res1.getResults().getString("value");
                        if (valueRaw != null) {
                            if (valueRaw.startsWith("{")) {
                                var.setValueRefined("[" + valueRaw.substring(1, valueRaw.length() - 1) + "]");
                            } else {
                                LOG.info("DBG Warn: can't determine expression result type: " + valueRaw);
                                var.setValueRefined(valueRaw);
                            }
                        } else {
                            LOG.info(String.format("DBG Error: Invalid debugger response for expression eval: %s", res1.toString()));
                            var.setError(PascalBundle.message("debug.expression.no.result"));
                            var.setChildrenCount(1);        // TODO: resolve as type first
                        }
                    }
                }
                );
            } else {
                process.sendCommand(String.format("-data-read-memory-bytes -o %d %s %d", start, var.getName(), end - start), res12 -> {
                    List<Object> memory = res12.getResults().getValue("memory") != null ? res12.getResults().getList("memory") : null;
                    GdbMiResults tuple = ((memory != null) && (memory.size() > 0)) ? (GdbMiResults) memory.get(0) : null;
                    String content = tuple != null ? tuple.getString("contents") : null;
                    if ((content != null) && (content.length() == ((end-start) * 2))) {
                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < end - start; i++) {
                            if (sb.length() > 1) {
                                sb.append(", ");
                            }
                            sb.append(DebugUtil.parseHex(content.substring(i * 2, i * 2 + 2)));
                        }
                        sb.append("]");
                        var.setValueRefined(sb.toString());
                    } else {
                        LOG.info(String.format("DBG Error: Invalid debugger response for memory: %s", res12.toString()));
                        var.setError(PascalBundle.message("debug.error.memory.read", var.getName()));
                    }
                });
            }
        }
        );
    }

    @Override
    public void addLineBreakpoint(String filename, int line, boolean temporary ,CommandSender.FinishCallback callback) {
        process.sendCommand(String.format("-break-insert %s %s \"%s:%d\"",
                process.isInferiorRunning() ? "-h " : "", temporary ? "-t" : "",
                getFileName(filename), line), callback);
    }

    @Override
    public void threadSelect(String id) {
        process.sendCommand("-thread-select " + id);
    }

    private void initPointerSize() {
        evaluate("sizeof (void*)", res -> {
            Integer pointerSize = DebugUtil.retrieveResultValueInt(res);
            if (null == pointerSize) {
                throw new RuntimeException("Can't get pointer size: " + res);
            }
            options.pointerSize = pointerSize;
        });
    }

}
