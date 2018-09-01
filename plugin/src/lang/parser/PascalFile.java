package com.siberika.idea.pascal.lang.parser;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public interface PascalFile extends PsiFile {
    PsiElement getImplementationSection();
}
