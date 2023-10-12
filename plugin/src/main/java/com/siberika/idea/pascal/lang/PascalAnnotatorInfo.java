package com.siberika.idea.pascal.lang;

import com.intellij.psi.PsiFile;

public class PascalAnnotatorInfo {
    private final PsiFile file;
    private final int lineCount;

    public PascalAnnotatorInfo(PsiFile file, int lineCount) {
        this.file = file;
        this.lineCount = lineCount;
    }

    public PsiFile getFile() {
        return file;
    }

    public int getLineCount() {
        return lineCount;
    }
}
