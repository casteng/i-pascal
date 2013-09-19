package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public interface PascalNamedElement extends PascalPsiElement, PsiNameIdentifierOwner {
    @NotNull
    String getName();
    String getNamespace();
    String getNamePart();
}
