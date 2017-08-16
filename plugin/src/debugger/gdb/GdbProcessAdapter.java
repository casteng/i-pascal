package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.MessageType;
import com.intellij.xdebugger.frame.XStackFrame;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiParser;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbStopReason;
import com.siberika.idea.pascal.jps.util.PascalConsoleProcessAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 28/03/2017
 */
public class GdbProcessAdapter extends PascalConsoleProcessAdapter {
    private static final Logger LOG = Logger.getInstance(GdbProcessAdapter.class);
    private final PascalXDebugProcess process;
    private GdbSuspendContext suspendContext;

    public GdbProcessAdapter(PascalXDebugProcess xDebugProcess) {
        this.process = xDebugProcess;
    }

    @Override
    public boolean onLine(String text) {
        GdbMiLine res = GdbMiParser.parseLine(text);
        if (GdbMiLine.Type.EXEC_ASYNC.equals(res.getType())) {
            if ("stopped".equals(res.getRecClass())) {
                handleStop(res);
            } else if ("running".equals(res.getRecClass())) {
                process.setInferiorRunning(true);
            }
        } else if (GdbMiLine.Type.RESULT_RECORD.equals(res.getType())) {
            if ("done".equals(res.getRecClass())) {
                if (res.getResults().getValue("stack") != null) {
                    addStackFramesToContainer(res.getResults().getList("stack"));
                } else if (res.getResults().getValue("bkpt") != null) {
                    process.getBreakpointHandler().handleBreakpointResult(res.getResults().getTuple("bkpt"));
                } else if (res.getResults().getValue("variables") != null) {
                    process.handleVariablesResponse(res.getResults().getList("variables"));
                } else if (isCreateVarResult(res.getResults())) {
                    process.handleVarResult(res.getResults());
                } else if (res.getResults().getValue("changelist") != null) {
                    process.handleVarUpdate(res.getResults());
                } else if (res.getResults().getValue("children") != null) {
                    process.handleChildrenResult(res.getResults().getList("children"));
                } else if ("0".equals(res.getResults().getString("numchild"))) {
                    process.handleChildrenResult(Collections.emptyList());
                }
            } else if ("error".equals(res.getRecClass())) {
                String msg = res.getResults().getString("msg");
                if (msg != null) {
                    process.getSession().reportMessage(PascalBundle.message("debug.error.response",
                            msg.replace("\\n", "\n")), MessageType.ERROR);
                }
            }
        }
        return true;
    }

    private boolean isCreateVarResult(GdbMiResults results) {
        return (results.getValue("name") != null) && (results.getValue("value") != null);
    }

    private void handleStop(GdbMiLine res) {
        suspendContext = new GdbSuspendContext(process, res);
        process.setInferiorRunning(false);
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
                case LOCATION_REACHED:
                case FUNCTION_FINISHED: {
                    msg = reason.getUid();
                    break;
                }
                case EXITED:
                case EXITED_SIGNALLED:
                case EXITED_NORMALLY: {
                    msg = reason.getUid();
                    process.sendCommand("-gdb-exit");
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
                frames.add(new GdbStackFrame((GdbExecutionStack) suspendContext.getActiveExecutionStack(), res.getTuple("frame")));
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
