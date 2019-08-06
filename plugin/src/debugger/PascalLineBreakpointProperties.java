package com.siberika.idea.pascal.debugger;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointProperties extends XBreakpointProperties<PascalLineBreakpointProperties> {
    private Integer requestedLine;

    public PascalLineBreakpointProperties() {
        this(null);
    }

    public PascalLineBreakpointProperties(Integer requestedLine) {
        this.requestedLine = requestedLine;
    }

    @Nullable
    @Override
    public PascalLineBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PascalLineBreakpointProperties state) {
    }

    public Integer getRequestedLine() {
        return requestedLine;
    }

    public void setRequestedLine(Integer requestedLine) {
        this.requestedLine = requestedLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PascalLineBreakpointProperties that = (PascalLineBreakpointProperties) o;
        return Objects.equals(getRequestedLine(), that.getRequestedLine());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequestedLine());
    }
}
