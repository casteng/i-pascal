package com.siberika.idea.pascal.debugger;

import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Transient;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointProperties extends XBreakpointProperties<PascalLineBreakpointProperties> {
    @OptionTag("filename")
    private String filename;
    @OptionTag("line")
    private Integer line;
    @Transient
    private boolean moving;

    public PascalLineBreakpointProperties() {
    }

    public PascalLineBreakpointProperties(String filename, Integer line) {
        this.filename = filename;
        this.line = line;
        this.moving = false;
    }

    @Nullable
    @Override
    public PascalLineBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PascalLineBreakpointProperties state) {
        filename = state.filename;
        line = state.line;
        moving = state.moving;
    }

    public String getFilename() {
        return filename;
    }

    public Integer getLine() {
        return line;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PascalLineBreakpointProperties that = (PascalLineBreakpointProperties) o;
        return Objects.equals(filename, that.filename) &&
                Objects.equals(line, that.line);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, line);
    }
}
