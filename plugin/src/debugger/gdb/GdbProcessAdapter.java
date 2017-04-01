package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.xdebugger.frame.XStackFrame;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiParser;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbStopReason;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 28/03/2017
 */
public class GdbProcessAdapter implements ProcessListener {
    private static final Logger LOG = Logger.getInstance(GdbProcessAdapter.class);
    private final PascalXDebugProcess process;
    private GdbSuspendContext suspendContext;

    public GdbProcessAdapter(PascalXDebugProcess xDebugProcess) {
        this.process = xDebugProcess;
    }

    @Override
    public void startNotified(ProcessEvent event) {
    }

    @Override
    public void processTerminated(ProcessEvent event) {
    }

    @Override
    public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
    }

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
        GdbMiLine res = GdbMiParser.parseLine(event.getText());
        if (GdbMiLine.Type.EXEC_ASYNC.equals(res.getType())) {
            if ("stopped".equals(res.getRecClass())) {
                handleStop(res);
            }
        } else if (GdbMiLine.Type.RESULT_RECORD.equals(res.getType())) {
            if ("done".equals(res.getRecClass())) {
                List<Object> stack = res.getResults().getList("stack");
                if (stack != null) {
                    addStackFramesToContainer(stack);
                }
            }
        }
    }

    private void handleStop(GdbMiLine res) {
        suspendContext = new GdbSuspendContext(process, res);
        process.getSession().positionReached(suspendContext);
        GdbStopReason reason = GdbStopReason.fromUid(res.getResults().getString("reason"));
        String msg = null;
        if (reason != null) {
            switch (reason) {
                case SIGNAL_RECEIVED: {
                    String detail = res.getResults().getString("signal-name");
                    msg = detail != null ? String.format(", %s (%s)", res.getResults().getValue("signal-name"), res.getResults().getValue("signal-meaning")) : "";
                    break;
                }
                case BREAKPOINT_HIT:
                case WATCHPOINT_TRIGGER:
                case READ_WATCHPOINT_TRIGGER:
                case ACCESS_WATCHPOINT_TRIGGER:
                case EXITED:
                case EXITED_SIGNALLED:
                case EXITED_NORMALLY:
                case LOCATION_REACHED:
                case FUNCTION_FINISHED: {
                    msg = reason.getUid();
                }
            }
            if (msg != null) {
                process.getSession().reportMessage(PascalBundle.message("debug.notify.stopped", msg), MessageType.INFO);
            }
        }
    }

    private void addStackFramesToContainer(List<Object> stack) {
        List<XStackFrame> frames = new ArrayList<XStackFrame>();
        for (Object o : stack) {
            if (o instanceof GdbMiResults) {
                GdbMiResults res = (GdbMiResults) o;
                frames.add(new GdbStackFrame(res.getTuple("frame")));
            } else {
                reportError("Invalid stack frames list entry");
                return;
            }
        }

        suspendContext.getStackFrameContainer().addStackFrames(frames, true);
    }

    private void reportError(String msg) {
        LOG.warn("ERROR: " + msg);
    }
}
