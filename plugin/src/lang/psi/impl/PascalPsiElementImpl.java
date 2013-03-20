package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalPsiElementImpl extends ASTWrapperPsiElement implements PascalPsiElement {
    public PascalPsiElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return getNode().getElementType().toString();
    }
}

