package com.siberika.idea.pascal.debugger.lldb;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.siberika.idea.pascal.debugger.CommandSender;
import com.siberika.idea.pascal.debugger.DebugBackend;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.GdbVariableObject;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;

public class LldbDebugBackend extends DebugBackend {

    private static final Logger LOG = Logger.getInstance(LldbDebugBackend.class);

    public LldbDebugBackend(PascalXDebugProcess process) {
        super(process);
    }

    @Override
    public void init() {
        options.supportsBulkDelete = true;
        options.useFullnameForBreakpoints = false;  // LLDB doesn't support full names in breakpoints
        process.sendCommand("-interpreter-exec console \"br delete\"");
        process.sendCommand("set set target.max-children-count " + options.limitElements);
    }

    @Override
    public void onSessionInit() {
        String runCommand = "-exec-run";
        if (getData().getBoolean(PascalSdkData.Keys.DEBUGGER_REDIRECT_CONSOLE)) {
            if (SystemInfo.isWindows) {
                process.sendCommand("-gdb-set new-console on");
            } else {
                runCommand = String.format("-interpreter-exec console \"process launch --stdout %1$s --stderr %1$s\"", process.getOutputFile().getAbsolutePath());
            }
        }
        process.sendCommand(runCommand);
        setupFormatters();
        initPointerSize();
    }

    @Override
    public void createVar(String key, String expression, CommandSender.FinishCallback finishCallback) {
        process.sendCommand(String.format("-var-create %s * \"%s\"", key, expression), finishCallback);
    }

    @Override
    public void queryArrayValue(GdbVariableObject var, int start, long end) {
        String deref = var.getType().contains("(*)") ? "[0]" : "";
        process.sendCommand(String.format("type summary add -s \"%s\\$${var[%d-%d]}\" -n %s", var.getKey(), start, end - 1, var.getKey()));
        process.sendCommand(String.format("fr v %s%s --summary %s", var.getName(), deref, var.getKey()));
    }

    private void initPointerSize() {
        process.sendCommand("-data-evaluate-expression \"sizeof (void*)\" --language c", res -> {
            if (res.getType() == GdbMiLine.Type.RESULT_RECORD && "done".equals(res.getRecClass())) {
                options.pointerSize = res.getResults().getInteger("value");
            }
        });
    }

    private void setupFormatters() {
        setupSummary("BYTE", "${var%u}");
        setupSummary("SHORTINT", "${var%d}");
        setupSummary("WIDECHAR", "${var%U}");
        setupSummary("UNICODECHAR", "${var%U}");
        process.sendCommand("type category enable Pascal");
    }

    private void setupSummary(String type, String summary) {
        String cmd = "type summary add -s \"" + summary + "\" " + type + " -p -w Pascal";
        process.sendCommand(cmd);
    }

}
