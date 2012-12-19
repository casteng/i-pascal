package com.siberika.idea.pascal.lang.parser.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalFileImpl extends PsiFileBase implements PascalFile {
    public PascalFileImpl(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, PascalLanguage.INSTANCE);
    }

    @Nullable
    @Override
    public String getModuleName() {
        return "Fixed module name";
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PascalFileType.INSTANCE;
    }
}
