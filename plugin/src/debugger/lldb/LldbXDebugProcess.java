package com.siberika.idea.pascal.debugger.lldb;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.xdebugger.XDebugSession;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.GdbProcessAdapter;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class LldbXDebugProcess extends PascalXDebugProcess {

    private static final Logger LOG = Logger.getInstance(LldbXDebugProcess.class);

    public LldbXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        super(session, environment, executionResult);
    }

    @Override
    public String getVarFrame() {
        return "*";
    }

    @Override
    public String getVarNameQuoteChar() {
        return "";
    }

    @Override
    protected String getPointerSizeCommand() {
        return "-data-evaluate-expression \"sizeof (void*)\" --language c";
    }

    @Override
    protected void init() {
        options.supportsBulkDelete = true;
        options.supportsSummary = true;
        if (isOutputConsoleNeeded()) {
            createOutputConsole();
        }
        console = (ConsoleView) executionResult.getExecutionConsole();

        sendCommand("-interpreter-exec console \"br delete\"");
    }

    @Override
    protected void applyLimits() {
        super.applyLimits();
        sendCommand("set set target.max-children-count " + options.limitElements);
    }

    @Override
    public void sessionInitialized() {
        getProcessHandler().addProcessListener(new GdbProcessAdapter(this));
        sendCommand("-gdb-set target-async on");
        String runCommand = "-exec-run";
        if (getData().getBoolean(PascalSdkData.Keys.DEBUGGER_REDIRECT_CONSOLE)) {
            if (SystemInfo.isWindows) {
                sendCommand("-gdb-set new-console on");
            } else {
                runCommand = String.format("-interpreter-exec console \"process launch --stdout %1$s --stderr %1$s\"", outputFile.getAbsolutePath());
            }
        }
        sendCommand(runCommand);
        getSession().setPauseActionSupported(true);
        setupFormatters();
        super.sessionInitialized();
    }

    private void setupFormatters() {
        setupSummary("BYTE", "${var%u}");
        setupSummary("SHORTINT", "${var%d}");
        setupSummary("WIDECHAR", "${var%U}");
        setupSummary("UNICODECHAR", "${var%U}");
        sendCommand("type category enable Pascal");
    }

    private void setupSummary(String type, String summary) {
        String cmd = "type summary add -s \"" + summary + "\" " + type + " -p -w Pascal";
        sendCommand(cmd);
    }

}
