package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public class PascalNamedElementImpl extends ASTWrapperPsiElement implements PascalNamedElement {
    private volatile String myCachedName;

    public PascalNamedElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myCachedName = null;
    }

    @NotNull
    @Override
    public String getName() {
        if (myCachedName == null) {
            myCachedName = getId().getText();
        }
        return myCachedName;
    }

    @NotNull
    @Override
    public PsiElement getId() {
        return findChildByType(PasTypes.NAMESPACE_NAME);
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException {
        getId().replace(PasElementFactory.createLeafFromText(getProject(), s));
        return this;
    }

    @Override
    public int getTextOffset() {
        return getId().getTextOffset();
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return new LocalSearchScope(getContainingFile());
    }

    @Override
    public PsiElement getNameIdentifier() {
        return getId();
    }

    @Override
    public String toString() {
        PsiElement nullableId = findChildByType(PasTypes.NAMESPACE_NAME);
        return super.toString() + ":" + (nullableId == null? null : nullableId.getText());
    }
}
