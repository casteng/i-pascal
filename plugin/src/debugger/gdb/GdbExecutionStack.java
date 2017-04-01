package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 31/03/2017
 */
public class GdbExecutionStack extends XExecutionStack {

    private final GdbSuspendContext context;
    private final GdbMiLine response;

    public GdbExecutionStack(GdbSuspendContext context, GdbMiLine response) {
        super("Thread: " + response.getResults().getString("thread-id"));
        this.context = context;
        this.response = response;
    }

    @Nullable
    @Override
    public XStackFrame getTopFrame() {
        GdbMiResults frame = response.getResults().getTuple("frame");
        return new GdbStackFrame(context.getProcess(), frame);
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        this.context.setStackFrameContainer(container);
        context.getProcess().sendCommand("-stack-list-frames");
    }
}
