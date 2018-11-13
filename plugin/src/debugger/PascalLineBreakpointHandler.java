package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<PascalLineBreakpointProperties>> {
    private final PascalXDebugProcess debugProcess;
    private final Map<PascalLineBreakpointProperties, Integer> breakIndexMap = new HashMap<PascalLineBreakpointProperties, Integer>();
    private final Set<PascalLineBreakpointProperties> registered = new HashSet<PascalLineBreakpointProperties>();
    private final Queue<XLineBreakpoint<PascalLineBreakpointProperties>> queue = new ArrayDeque<XLineBreakpoint<PascalLineBreakpointProperties>>();

    public PascalLineBreakpointHandler(PascalXDebugProcess debugProcess) {
        super(PascalLineBreakpointType.class);
        this.debugProcess = debugProcess;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint) {
        PascalLineBreakpointProperties props = breakpoint.getProperties();
        int line = breakpoint.getLine()+1;
        String filename = breakpoint.getPresentableFilePath();
        if (props != null) {
            if (props.isMoving()) {
                return;
            }
            line = props.getLine();
            filename = props.getFilename();
        }
        PascalLineBreakpointProperties key = new PascalLineBreakpointProperties(filename, line);
        registered.add(key);
        queue.add(breakpoint);
        if (!PascalXDebugProcess.getData(PascalXDebugProcess.retrieveSdk(debugProcess.environment)).getBoolean(PascalSdkData.Keys.DEBUGGER_BREAK_FULL_NAME)) {
            filename = FileUtil.getFilename(filename);
        }
        debugProcess.sendCommand(String.format("-break-insert %s -f \"%s:%d\"", debugProcess.isInferiorRunning() ? "-h" : "", filename, line));
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint, boolean temporary) {
        PascalLineBreakpointProperties props = breakpoint.getProperties();
        props = props != null ? props : new PascalLineBreakpointProperties(breakpoint.getPresentableFilePath(), breakpoint.getLine()+1);
        if (props.isMoving()) {
            return;
        }
        registered.remove(props);
        queue.poll();
        Integer ind = breakIndexMap.get(props);
        if (ind != null) {
            debugProcess.sendCommand(String.format("-break-delete %d", ind));
        } else {
            debugProcess.getSession().reportMessage(PascalBundle.message("debug.breakpoint.notFound"), MessageType.ERROR);
        }
    }

    public void handleBreakpointResult(GdbMiResults bp) {
        String fname = bp.getString("fullname");
        final String fullname = fname != null ? fname.replace("//", "/") : null;
        final Integer line = bp.getInteger("line");
        if (fullname != null && line != null && (line.compareTo(0) > 0)) {
            final XLineBreakpoint<PascalLineBreakpointProperties> lastRegistered = queue.poll();
            Integer requestedLine = getRequestedLine(bp);
            final PascalLineBreakpointProperties props = new PascalLineBreakpointProperties(fullname, line);
            if (!line.equals(requestedLine) && lastRegistered != null) {
                final XBreakpointManager manager = XDebuggerManager.getInstance(debugProcess.environment.getProject()).getBreakpointManager();
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            @Override
                            public void run() {
                                moveBreakpoint(manager, lastRegistered, fullname, line);
                            }
                        });
                    }
                });
            }
            breakIndexMap.put(props, bp.getInteger("number"));
        }
    }

    private void moveBreakpoint(XBreakpointManager manager, XLineBreakpoint<PascalLineBreakpointProperties> breakpoint, String fullname, Integer line) {
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

    private static final Pattern PATTERN_LINE = Pattern.compile(".*:(\\d+)");
    private Integer getRequestedLine(GdbMiResults bp) {
        String loc = bp.getString("original-location");
        Integer line = null;
        if (loc != null) {
            Matcher m = PATTERN_LINE.matcher(loc);
            if (m.matches()) {
                line = Integer.parseInt(m.group(1));
            }
        }
        return line;
    }
}
