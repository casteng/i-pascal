package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.ui.MessageType;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.siberika.idea.pascal.debugger.gdb.PascalXDebugProcess;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<PascalLineBreakpointProperties>> {
    private final PascalXDebugProcess debugProcess;

    public PascalLineBreakpointHandler(PascalXDebugProcess debugProcess) {
        super(PascalLineBreakpointType.class);
        this.debugProcess = debugProcess;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint) {
        debugProcess.getSession().reportMessage("Breakpoint add", MessageType.INFO);
        debugProcess.sendCommand(String.format("-break-insert %s:%d", breakpoint.getPresentableFilePath(), breakpoint.getLine()));
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint, boolean temporary) {
        debugProcess.getSession().reportMessage("Breakpoint remove", MessageType.INFO);
    }
}
