package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.siberika.idea.pascal.debugger.gdb.GdbSuspendContext;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.jps.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<PascalLineBreakpointProperties>> {

    private static final Logger LOG = Logger.getInstance(PascalLineBreakpointHandler.class);

    private final PascalXDebugProcess debugProcess;
    private final Map<PascalLineBreakpointProperties, Integer> breakIndexMap = new HashMap<>();
    private final Map<Integer, XLineBreakpoint<PascalLineBreakpointProperties>> indexBreakMap = new HashMap<>();

    public PascalLineBreakpointHandler(PascalXDebugProcess debugProcess) {
        super(PascalLineBreakpointType.class);
        this.debugProcess = debugProcess;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint) {
        PascalLineBreakpointProperties props = breakpoint.getProperties();
        Integer reqLine = props.getRequestedLine();
        if (null == reqLine) {
            props.setRequestedLine(breakpoint.getLine() + 1);
        } else if (reqLine != breakpoint.getLine() + 1) {                // Moving breakpoints due to source code move
            ApplicationManager.getApplication().invokeLater(() ->
                    ApplicationManager.getApplication().runWriteAction(() ->
                            XDebuggerManager.getInstance(debugProcess.getProject()).getBreakpointManager().removeBreakpoint(breakpoint))
            );
            return;
        }
        String filename = breakpoint.getPresentableFilePath();
        if (!debugProcess.backend.options.useFullnameForBreakpoints) {
            filename = FileUtil.getFilename(filename);
        }
        debugProcess.sendCommand(String.format("-break-insert %s -f \"%s:%d\"", debugProcess.isInferiorRunning() ? "-h" : "", filename, props.getRequestedLine()),
                new CommandSender.FinishCallback() {
                    @Override
                    public void call(GdbMiLine res) {
                        if (res.getResults().getValue("bkpt") != null) {
                            handleBreakpointResult(res.getResults().getTuple("bkpt"), breakpoint);
                        }
                    }
                });
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint, boolean temporary) {
        PascalLineBreakpointProperties props = breakpoint.getProperties();
        props = props != null ? props : new PascalLineBreakpointProperties(breakpoint.getLine() + 1);
        Integer ind = breakIndexMap.get(props);
        if (ind != null) {
            debugProcess.sendCommand(String.format("-break-delete %d", ind));
            breakIndexMap.remove(props);
            indexBreakMap.remove(ind);
        } else {
            LOG.info(String.format("DBG Warn: breakpoint not found at: %s:%d", breakpoint.getShortFilePath(), breakpoint.getLine()));
        }
    }

    public void handleBreakpointResult(GdbMiResults bp, XLineBreakpoint<PascalLineBreakpointProperties> breakpoint) {
        String fname = bp.getString("fullname");
        final String fullname = fname != null ? fname.replace("//", "/") : null;
        final Integer line = bp.getInteger("line");
        boolean bpValid = (fullname != null) && !"??".equals(fullname) && (line != null) && (line > 0);
        final Integer number = bp.getInteger("number");
        if (number != null) {
            breakIndexMap.put(breakpoint.getProperties(), number);
            indexBreakMap.put(number, breakpoint);
            if (!bpValid) {
                debugProcess.getSession().setBreakpointInvalid(breakpoint, "No code");
                return;
            }
            Integer requestedLine = breakpoint.getLine() + 1;
            if (!line.equals(requestedLine)) {              // breakpoint location moved by debugger
                debugProcess.getSession().setBreakpointInvalid(breakpoint, "No code");
                debugProcess.sendCommand(String.format("-break-delete %d", number));
            }
        }
    }

    public void handleBreakpointHit(GdbMiLine res, GdbSuspendContext suspendContext) {
        Integer breakId = res.getResults().getInteger("bkptno");
        if (null == breakId) {
            LOG.info("DBG Error: invalid breakpoint ID: " + res);
        }
        XLineBreakpoint<PascalLineBreakpointProperties> breakpoint = indexBreakMap.get(breakId);
            if (breakpoint != null) {
                debugProcess.getSession().breakpointReached(breakpoint, null, suspendContext);
            }
    }
}
