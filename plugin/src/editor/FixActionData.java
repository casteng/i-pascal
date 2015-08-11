package com.siberika.idea.pascal.editor;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;

/**
 * Author: George Bakhtadze
 * Date: 24/03/2015
 */
public abstract class FixActionData {
    PsiElement parent;
    String text = null;
    int offset = 0;

    abstract void calcData(final PsiElement section, final PascalNamedElement element);

}
