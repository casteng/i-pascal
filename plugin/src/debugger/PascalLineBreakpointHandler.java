package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<PascalLineBreakpointProperties>> {
    private final PascalXDebugProcess debugProcess;
    private final Map<PascalLineBreakpointProperties, Integer> breakIndexMap = new HashMap<>();
    private final Set<PascalLineBreakpointProperties> registered = new HashSet<>();

    public PascalLineBreakpointHandler(PascalXDebugProcess debugProcess) {
        super(PascalLineBreakpointType.class);
        this.debugProcess = debugProcess;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint) {
        PascalLineBreakpointProperties props = breakpoint.getProperties();
        int line = breakpoint.getLine() + 1;
        String filename = breakpoint.getPresentableFilePath();
        if (props != null && props.getLine() != null) {
            if (props.isMoving()) {
                return;
            }
            line = props.getLine();
            filename = props.getFilename();
        }
        PascalLineBreakpointProperties properties = new PascalLineBreakpointProperties(filename, line);
        registered.add(properties);
        PascalSdkData data = debugProcess.getData();
        if (data.isLldbBackend() || !data.getBoolean(PascalSdkData.Keys.DEBUGGER_BREAK_FULL_NAME)) {  // LLDB doesn't support full names in breakpoints
            filename = FileUtil.getFilename(filename);
        }
        debugProcess.sendCommand(String.format("-break-insert %s -f \"%s:%d\"", debugProcess.isInferiorRunning() ? "-h" : "", filename, line),
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
        props = props != null ? props : new PascalLineBreakpointProperties(breakpoint.getPresentableFilePath(), breakpoint.getLine() + 1);
        if (props.isMoving()) {
            return;
        }
        registered.remove(props);
        Integer ind = breakIndexMap.get(props);
        if (ind != null) {
            debugProcess.sendCommand(String.format("-break-delete %d", ind));
        } else {
            debugProcess.getSession().reportMessage(PascalBundle.message("debug.breakpoint.notFound"), MessageType.ERROR);
        }
    }

    public void handleBreakpointResult(GdbMiResults bp, XLineBreakpoint<PascalLineBreakpointProperties> breakpoint) {
        String fname = bp.getString("fullname");
        final String fullname = fname != null ? fname.replace("//", "/") : null;
        final Integer line = bp.getInteger("line");
        boolean bpValid = (fullname != null) && !"??".equals(fullname) && (line != null) && (line > 0);
        breakIndexMap.put(breakpoint.getProperties(), bp.getInteger("number"));
        if (!bpValid) {
            breakpoint.setEnabled(false);
            return;
        }
        Integer requestedLine = breakpoint.getLine() + 1;
        if (!line.equals(requestedLine)) {              // breakpoint location moved by debugger
            moveBreakpoint(breakpoint, fullname, line);
        }
    }

    private void moveBreakpoint(XLineBreakpoint<PascalLineBreakpointProperties> breakpoint, String fullname, Integer line) {
        final XBreakpointManager manager = XDebuggerManager.getInstance(debugProcess.environment.getProject()).getBreakpointManager();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        PascalLineBreakpointProperties oldProps = breakpoint.getProperties();
                        final PascalLineBreakpointProperties props = new PascalLineBreakpointProperties(fullname, line);
                        oldProps.setMoving(true);
                        props.setMoving(true);
                        try {
                            manager.removeBreakpoint(breakpoint);
                            if (!registered.contains(props)) {
                                manager.addLineBreakpoint(breakpoint.getType(), breakpoint.getFileUrl(), line - 1, props);
                                registered.add(props);
                            }
                        } finally {
                            oldProps.setMoving(false);
                            props.setMoving(false);
                        }
                    }
                });
            }
        });
    }

}
