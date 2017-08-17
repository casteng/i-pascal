package com.siberika.idea.pascal.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.siberika.idea.pascal.run.PascalRunConfiguration;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalDebugRunner extends GenericProgramRunner {
    @NotNull
    public String getRunnerId() {
        return "com.siberika.idea.pascal.run.PascalDebugRunner";
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state,
                                             @NotNull final ExecutionEnvironment environment) throws ExecutionException {
        XDebuggerManager xDebuggerManager = XDebuggerManager.getInstance(environment.getProject());

        final ExecutionResult executionResult = state.execute(environment.getExecutor(), this);

        final PascalRunConfiguration conf = (PascalRunConfiguration) environment.getRunProfile();

        return xDebuggerManager.startSession(environment, new XDebugProcessStarter() {
            @NotNull
            @Override
            public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
                return PascalDebugFactory.createXDebugProcess(conf.getSdk(), session, environment, executionResult);
            }
        }).getRunContentDescriptor();
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (profile instanceof PascalRunConfiguration) {
            Module module = ((PascalRunConfiguration) profile).getConfigurationModule().getModule();
            Sdk sdk = module != null ? ModuleRootManager.getInstance(module).getSdk() : null;
            return ((null == sdk) || (sdk.getSdkType() instanceof FPCSdkType)) &&
                    executorId.equals(DefaultDebugExecutor.EXECUTOR_ID);
        } else {
            return false;
        }

    }

}
