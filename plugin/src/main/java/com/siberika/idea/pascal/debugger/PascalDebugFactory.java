package com.siberika.idea.pascal.debugger;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;

public class PascalDebugFactory {

    static XDebugProcess createXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        return new PascalXDebugProcess(session, environment, executionResult);
    }

    public static void adjustCommand(Sdk sdk, GeneralCommandLine commandLine, String executable) {
        PascalSdkData data = sdk != null ? BasePascalSdkType.getAdditionalData(sdk) : PascalSdkData.EMPTY;
        if (DebugUtil.isLldb(sdk)) {
            adjustCommandLldb(sdk, data, commandLine, executable);
        } else {
            adjustCommandGdb(sdk, data, commandLine, executable);
        }
    }

    private static void adjustCommandLldb(Sdk sdk, PascalSdkData data, GeneralCommandLine commandLine, String executable) {
        String command = BasePascalSdkType.getDebuggerCommand(sdk, PascalSdkData.getDefaultLLDBCommand());
        commandLine.setExePath(command);
        commandLine.addParameter(executable);
    }

    private static void adjustCommandGdb(Sdk sdk, PascalSdkData data, GeneralCommandLine commandLine, String executable) {
        String command = BasePascalSdkType.getDebuggerCommand(sdk, "gdb");
        commandLine.setExePath(command);
        if (!data.getBoolean(PascalSdkData.Keys.DEBUGGER_USE_GDBINIT)) {
            commandLine.addParameters("-n");
            commandLine.addParameters("-fullname");
            commandLine.addParameters("-nowindows");
            commandLine.addParameters("-interpreter=mi");
        }

        if (data.getValue(PascalSdkData.Keys.DEBUGGER_OPTIONS.getKey()) != null) {
            String[] compilerOptions = data.getString(PascalSdkData.Keys.DEBUGGER_OPTIONS).split("\\s+");
            commandLine.addParameters(compilerOptions);
        }

        commandLine.addParameters("--args");
        commandLine.addParameters(executable);
    }

}
