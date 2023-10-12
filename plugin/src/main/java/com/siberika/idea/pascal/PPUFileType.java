package com.siberika.idea.pascal;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: George Bakhtadze
 * Date: 09.12.2012
 */
public class PPUFileType implements FileType {
    public static final PPUFileType INSTANCE = new PPUFileType();

    protected PPUFileType() {
    }

    @NotNull
    @Override
    public String getName() {
        return "FPC_PPU";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Free Pascal compiled unit";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "ppu";
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
