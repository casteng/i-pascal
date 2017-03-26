package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.ui.MessageType;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<PascalLineBreakpointProperties>> {
    private final PascalXDebugProcess myDebugProcess;

    public PascalLineBreakpointHandler(PascalXDebugProcess debugProcess) {
        super(PascalLineBreakpointType.class);
        myDebugProcess = debugProcess;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint) {
        myDebugProcess.getSession().reportMessage("Breakpoint add", MessageType.INFO);
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<PascalLineBreakpointProperties> breakpoint, boolean temporary) {
        myDebugProcess.getSession().reportMessage("Breakpoint remove", MessageType.INFO);
    }
}
