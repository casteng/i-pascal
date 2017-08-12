package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 31/03/2017
 */
public class GdbSuspendContext extends XSuspendContext {
    private final PascalXDebugProcess process;
    private final XExecutionStack stack;
    private XExecutionStack.XStackFrameContainer stackFrameContainer;

    public GdbSuspendContext(PascalXDebugProcess process, GdbMiLine line) {
        this.process = process;
        this.stack = new GdbExecutionStack(this, line);
    }

    @Nullable
    @Override
    public XExecutionStack getActiveExecutionStack() {
        return stack;
    }

    @NotNull
    @Override
    public XExecutionStack[] getExecutionStacks() {
        return super.getExecutionStacks();
    }

    public PascalXDebugProcess getProcess() {
        return process;
    }

    public XExecutionStack.XStackFrameContainer getStackFrameContainer() {
        return stackFrameContainer;
    }

    public void setStackFrameContainer(XExecutionStack.XStackFrameContainer stackFrameContainer) {
        this.stackFrameContainer = stackFrameContainer;
    }
}
