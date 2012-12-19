package com.siberika.idea.pascal.lang.parser;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public interface PascalFile extends PsiFile {
    @Nullable
    String getModuleName();
}
