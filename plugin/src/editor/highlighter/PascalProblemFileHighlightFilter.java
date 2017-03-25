package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalFileType;

public class PascalProblemFileHighlightFilter implements Condition<VirtualFile> {
    @Override
    public boolean value(VirtualFile virtualFile) {
        return virtualFile.getFileType() == PascalFileType.INSTANCE;
    }
}
