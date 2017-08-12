package com.siberika.idea.pascal.debugger.lldb;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.XDebugSession;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class LldbXDebugProcess extends PascalXDebugProcess {

    private static final Logger LOG = Logger.getInstance(LldbXDebugProcess.class);

    private static final String LLDB_MI_PATH = "/Applications/Xcode.app/Contents/Developer/usr/bin/lldb-mi";
    private static final String LLDB_DEBUGSERVER_PATH = "/Applications/Xcode.app/Contents/SharedFrameworks/LLDB.framework/Versions/Current/Resources/debugserver";

    public LldbXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        super(session, environment, executionResult);
    }

    @Override
    protected void init(ExecutionEnvironment environment) {

    }

    @Override
    protected void doCreateTabLayouter(RunnerLayoutUi ui) {

    }
}
