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
        process.sendCommand("set print elements " + options.limitElements);
        process.sendCommand("set max-value-size " + options.limitValueSize);
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
        process.sendCommand(String.format("-var-create \"%s\" @ \"%s\"", key, expression), finishCallback);
    }

    @Override
    public void queryArrayValue(GdbVariableObject var, int start, long end) {
        String deref = var.getType().contains("(*)") ? "*" : "";
        process.sendCommand(String.format("-data-evaluate-expression sizeof(%s%s)[0]", deref, var.getName()), new CommandSender.FinishCallback() {
                    @Override
                    public void call(GdbMiLine res) {
                        Integer elSize = DebugUtil.retrieveResultValueInt(res);
                        if (null == elSize) {
                            var.setError(PascalBundle.message("debug.expression.array.size.error"));
                            return;
                        }
                        if (elSize > 1) {                           // Normal array data output
                            process.sendCommand(String.format("-data-evaluate-expression *&(%s%s)[%d]@%d", deref, var.getName(), start, end - start), new CommandSender.FinishCallback() {
                                        @Override
                                        public void call(GdbMiLine res) {
                                            if (res.getType() == GdbMiLine.Type.RESULT_RECORD && "done".equals(res.getRecClass())) {
                                                String valueRaw = res.getResults().getString("value");
                                                if (valueRaw != null) {
                                                    if (valueRaw.startsWith("{")) {
                                                        var.setValueRefined("[" + valueRaw.substring(1, valueRaw.length() - 1) + "]");
                                                    } else {
                                                        LOG.info("DBG Warn: can't determine expression result type: " + valueRaw);
                                                        var.setValueRefined(valueRaw);
                                                    }
                                                } else {
                                                    LOG.info(String.format("DBG Error: Invalid debugger response for expression eval: %s", res.toString()));
                                                    var.setError(PascalBundle.message("debug.expression.no.result"));
                                                }
                                            }
                                        }
                                    }
                            );
                        } else {
                            process.sendCommand(String.format("-data-read-memory-bytes -o %d %s %d", start, var.getName(), end - start), new CommandSender.FinishCallback() {
                                @Override
                                public void call(GdbMiLine res) {
                                    List<Object> memory = res.getResults().getValue("memory") != null ? res.getResults().getList("memory") : null;
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
                                        LOG.info(String.format("DBG Error: Invalid debugger response for memory: %s", res.toString()));
                                        var.setError(PascalBundle.message("debug.error.memory.read", var.getName()));
                                    }
                                }
                            });
                        }
                    }
                }
        );
    }

    private void initPointerSize() {
        process.sendCommand("-data-evaluate-expression --language c \"sizeof (void*)\"", res -> {
            Integer pointerSize = DebugUtil.retrieveResultValueInt(res);
            if (null == pointerSize) {
                throw new RuntimeException("Can't get pointer size: " + res);
            }
            options.pointerSize = pointerSize;
        });
    }

}
