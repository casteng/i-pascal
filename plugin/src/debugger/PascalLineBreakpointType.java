package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.siberika.idea.pascal.PascalFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointType extends XLineBreakpointType<PascalLineBreakpointProperties> {
    static final String ID = "PascalLineBreakpoint";
    private static final String NAME = "Line breakpoint";

    protected PascalLineBreakpointType() {
        super(ID, NAME);
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        if (file.getFileType() != PascalFileType.INSTANCE) return false;
        return true;
    }

    @Nullable
    @Override
    public PascalLineBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        return new PascalLineBreakpointProperties(file.getCanonicalPath(), line+1);
    }
}
