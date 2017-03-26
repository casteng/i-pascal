package com.siberika.idea.pascal.debugger;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointProperties extends XBreakpointProperties<PascalLineBreakpointProperties> {
    @Nullable
    @Override
    public PascalLineBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(PascalLineBreakpointProperties state) {
    }
}
