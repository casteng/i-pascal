package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.PsiElement;

/**
 * Author: George Bakhtadze
 * Date: 01/03/2015
 */
public class PasInvalidElementException extends RuntimeException {
    private final PsiElement element;
    public PasInvalidElementException(PsiElement element) {
        this.element = element;
    }
}
