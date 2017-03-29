package com.siberika.idea.pascal.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.run.PascalRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalXDebugProcess extends XDebugProcess {

    private final XBreakpointHandler<?>[] MY_BREAKPOINT_HANDLERS = new XBreakpointHandler[] {new PascalLineBreakpointHandler(this)};
    private final ExecutionEnvironment environment;
    private final ExecutionResult executionResult;
    private ConsoleView myExecutionConsole;

    public PascalXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        super(session);
        this.environment = environment;
        this.executionResult = executionResult;
        try {
            createGdbProcess(environment);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void createGdbProcess(ExecutionEnvironment env) throws ExecutionException {
        RunProfile profile = env.getRunProfile();
        if (profile instanceof PascalRunConfiguration) {
            
        }
    }

    @Nullable
    @Override
    protected ProcessHandler doGetProcessHandler() {
        return executionResult.getProcessHandler();
    }

    @NotNull
    @Override
    public ExecutionConsole createConsole() {
        myExecutionConsole = (ConsoleView) executionResult.getExecutionConsole();
        return myExecutionConsole;
    }

    public void printToConsole(String text, ConsoleViewContentType contentType) {
        myExecutionConsole.print(text, contentType);
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
        getProcessHandler().addProcessListener(new GdbProcessAdapter());
        sendCommand("-gdb-set target-async on");
        sendCommand("-exec-run");
        getSession().setPauseActionSupported(true);
    }

    @Override
    public void startPausing() {
        printToConsole("Pausing", ConsoleViewContentType.NORMAL_OUTPUT);
        sendCommand("-exec-interrupt");
        getSession().positionReached(new XSuspendContext() {
            @Nullable
            @Override
            public XExecutionStack getActiveExecutionStack() {
                return super.getActiveExecutionStack();
            }

            @NotNull
            @Override
            public XExecutionStack[] getExecutionStacks() {
                return super.getExecutionStacks();
            }

            @Override
            public void computeExecutionStacks(XExecutionStackContainer container) {
                super.computeExecutionStacks(container);
            }
        });
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        sendCommand("-exec-continue");
    }

    void sendCommand(String command) {
        try {
            OutputStream commandStream = getProcessHandler().getProcessInput();
            if (commandStream != null) {
                commandStream.write((command + "\n").getBytes("UTF-8"));
                commandStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return new XDebuggerEditorsProvider() {
            @NotNull
            @Override
            public FileType getFileType() {
                return PascalFileType.INSTANCE;
            }

            @NotNull
            @Override
            public Document createDocument(@NotNull Project project,
                                           @NotNull String text,
                                           @Nullable XSourcePosition sourcePosition,
                                           @NotNull EvaluationMode mode) {
                LightVirtualFile file = new LightVirtualFile("_debug.pas", text);
                return FileDocumentManager.getInstance().getDocument(file);
            }
        };
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return MY_BREAKPOINT_HANDLERS;
    }

}
