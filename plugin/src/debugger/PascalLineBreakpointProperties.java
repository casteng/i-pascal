package com.siberika.idea.pascal.debugger;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointProperties extends XBreakpointProperties<PascalLineBreakpointProperties> {
    private String filename;
    private int line;
    private boolean moving;

    public PascalLineBreakpointProperties() {
    }

    public PascalLineBreakpointProperties(String filename, int line) {
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
    public void loadState(PascalLineBreakpointProperties state) {
    }

    public String getFilename() {
        return filename;
    }

    public int getLine() {
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

        if (line != that.line) return false;
        return filename != null ? filename.equals(that.filename) : that.filename == null;
    }

    @Override
    public int hashCode() {
        int result = filename != null ? filename.hashCode() : 0;
        result = 31 * result + line;
        return result;
    }
}
