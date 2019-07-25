package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 31/03/2017
 */
public class GdbExecutionStack extends XExecutionStack {

    private final GdbSuspendContext context;
    private final GdbStackFrame stoppedFrame;
    private final String threadId;
    private XStackFrameContainer stackFrameContainer;

    public GdbExecutionStack(GdbSuspendContext context, GdbMiLine response) {
        super("Thread: " + response.getResults().getString("thread-id"));
        this.context = context;
        this.stoppedFrame = new GdbStackFrame(this, response.getResults().getTuple("frame"));
        this.threadId = response.getResults().getString("thread-id");
    }

    @Nullable
    @Override
    public XStackFrame getTopFrame() {
        return stoppedFrame;
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        this.stackFrameContainer = container;
        context.getProcess().sendCommand("-stack-list-frames");
    }

    public PascalXDebugProcess getProcess() {
        return context.getProcess();
    }

    public String getThreadId() {
        return threadId;
    }

    public void addStackFrames(List<XStackFrame> frames) {
        if (!frames.isEmpty()) {
            frames.set(0, stoppedFrame);
        }
        stackFrameContainer.addStackFrames(frames, true);
    }

}
