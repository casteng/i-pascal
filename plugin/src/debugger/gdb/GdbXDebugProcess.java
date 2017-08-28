package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.xdebugger.XDebugSession;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;

import java.util.HashMap;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class GdbXDebugProcess extends PascalXDebugProcess {

    private static final Logger LOG = Logger.getInstance(GdbXDebugProcess.class);

    public GdbXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        super(session, environment, executionResult);
    }

    @Override
    protected String getVarFrame() {
        return "@";
    }

    @Override
    protected String getVarNameQuoteChar() {
        return "\"";
    }

    @Override
    protected void init() {
        try {
            createGdbProcess();
        } catch (ExecutionException e) {
            LOG.warn("Error running GDB", e);
        }
    }

    private void createGdbProcess() throws ExecutionException {
        if (isOutputConsoleNeeded()) {
            createOutputConsole();
        }
        console = (ConsoleView) executionResult.getExecutionConsole();
        variableObjectMap = new HashMap<String, GdbVariableObject>();
        sendCommand("-break-delete");
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
        getProcessHandler().addProcessListener(new GdbProcessAdapter(this));
        sendCommand("-gdb-set target-async on");
        if (getData().getBoolean(PascalSdkData.Keys.DEBUGGER_REDIRECT_CONSOLE)) {
            if (SystemInfo.isWindows) {
                sendCommand("-gdb-set new-console on");
            } else {
                sendCommand("-exec-arguments > " + outputFile.getAbsolutePath());
            }
        } 
        sendCommand("-exec-run");
        getSession().setPauseActionSupported(true);
    }

}
