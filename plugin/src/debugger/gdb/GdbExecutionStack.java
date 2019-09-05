package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.siberika.idea.pascal.debugger.CommandSender;
import com.siberika.idea.pascal.debugger.DebugThread;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 31/03/2017
 */
public class GdbExecutionStack extends XExecutionStack {

    private static final Logger LOG = Logger.getInstance(GdbExecutionStack.class);

    private final GdbSuspendContext context;
    private final GdbStackFrame stoppedFrame;
    private final Integer threadId;

    public GdbExecutionStack(GdbSuspendContext context, GdbMiLine response) {
        super("Thread: " + response.getResults().getString("thread-id"));
        this.context = context;
        this.threadId = response.getResults().getInteger("thread-id");
        this.stoppedFrame = new GdbStackFrame(this.getProcess(), response.getResults().getTuple("frame"), threadId);
    }

    public GdbExecutionStack(GdbSuspendContext context, DebugThread thread) {
        super(String.format("Thread #%s -%s (%s)", thread.getId(), thread.getName(), thread.getState().name()));
        this.context = context;
        this.threadId = thread.getId();
        this.stoppedFrame = thread.getFrame();
    }

    @Nullable
    @Override
    public XStackFrame getTopFrame() {
        return stoppedFrame;
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        context.getProcess().backend.threadSelect(String.valueOf(threadId));
        context.getProcess().sendCommand("-stack-list-frames " + firstFrameIndex + " " + getProcess().backend.options.maxFrames, new CommandSender.FinishCallback() {
            @Override
            public void call(GdbMiLine res) {
                if (res.getResults().getValue("stack") != null) {                   // -stack-list-frames result
                    addStackFramesToContainer(container, res.getResults().getList("stack"));
                }
            }
        });
    }

    public PascalXDebugProcess getProcess() {
        return context.getProcess();
    }

    public Integer getThreadId() {
        return threadId;
    }

    private void addStackFramesToContainer(XStackFrameContainer frameContainer, List<Object> stack) {
        List<XStackFrame> frames = new ArrayList<>(stack.size());
        for (Object o : stack) {
            if (o instanceof GdbMiResults) {
                GdbMiResults res = (GdbMiResults) o;
                frames.add(new GdbStackFrame(getProcess(), res.getTuple("frame"), getThreadId()));
            } else {
                LOG.info("DBG Warn: Invalid stack frames list entry");
                return;
            }
        }
        frameContainer.addStackFrames(frames, true);
    }

}
