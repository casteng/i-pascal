package com.siberika.idea.pascal.debugger;

import com.intellij.execution.runners.ExecutionEnvironment;
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
import com.siberika.idea.pascal.PascalFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalXDebugProcess extends XDebugProcess {
    public PascalXDebugProcess(XDebugSession session, ExecutionEnvironment environment) {
        super(session);
    }

    private XBreakpointHandler<?>[] myBreakpointHandlers = new XBreakpointHandler[] {new PascalLineBreakpointHandler(this)};

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
                LightVirtualFile file = new LightVirtualFile("_debugger.pas", text);
                return FileDocumentManager.getInstance().getDocument(file);
            }
        };
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return myBreakpointHandlers;
    }

}
