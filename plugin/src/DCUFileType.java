package com.siberika.idea.pascal;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: George Bakhtadze
 * Date: 21.05.2015
 */
public class DCUFileType implements FileType {
    public static final DCUFileType INSTANCE = new DCUFileType();

    protected DCUFileType() {
    }

    @NotNull
    @Override
    public String getName() {
        return "DELPHI_DCU";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Delphi compiled unit";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "dcu";
    }

    @Override
    public Icon getIcon() {
        return PascalIcons.COMPILED;
    }

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Nullable
    @Override
    public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
        return null;
    }

}
