package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.ui.MessageType;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.debugger.gdb.GdbXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<PascalLineBreakpointProperties>> {
    private final GdbXDebugProcess debugProcess;
    final Map<PascalLineBreakpointProperties, Integer> breakIndexMap = new HashMap<PascalLineBreakpointProperties, Integer>();

    public PascalLineBreakpointHandler(GdbXDebugProcess debugProcess) {
        super(PascalLineBreakpointType.class);
        this.debugProcess = debugProcess;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint) {
        PascalLineBreakpointProperties props = breakpoint.getProperties();
        int line = breakpoint.getLine();
        String filename = breakpoint.getPresentableFilePath();
        if (props != null) {
            line = props.getLine();
            filename = props.getFilename();
        }
        debugProcess.sendCommand(String.format("-break-insert %s:%d", filename, line));
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint, boolean temporary) {
        PascalLineBreakpointProperties props = breakpoint.getProperties();
        props = props != null ? props : new PascalLineBreakpointProperties(breakpoint.getPresentableFilePath(), breakpoint.getLine());
        Integer ind = breakIndexMap.get(props);
        if (ind != null) {
            debugProcess.sendCommand(String.format("-break-delete %d", ind));
        } else {
            debugProcess.getSession().reportMessage(PascalBundle.message("debug.breakpoint.notFound"), MessageType.ERROR);
        }
    }

    public void handleBreakpointResult(GdbMiResults bp) {
        PascalLineBreakpointProperties props = new PascalLineBreakpointProperties(bp.getString("fullname"), bp.getInteger("line"));
        breakIndexMap.put(props, bp.getInteger("number"));
    }
}
