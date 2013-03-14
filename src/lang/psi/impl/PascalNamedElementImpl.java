package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
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
        if ((myCachedName == null) || (myCachedName.length() == 0)) {
            PsiElement element = getNameElement();
            myCachedName = element != null ? element.getText() : "";
        }
        return myCachedName;
    }

    public PsiElement getNameElement() {
        PsiElement result = findChildByType(PasTypes.NAMESPACE_NAME);
        if (null == result) {
            result = findChildByType(PasTypes.NAME);
        }
        return result;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException {
        PsiElement element = getNameElement();
        if (element != null) {
            element.replace(PasElementFactory.createLeafFromText(getProject(), s));
        }
        return this;
    }

    @Override
    public int getTextOffset() {
        PsiElement element = getNameElement();
        return element != null ? element.getTextOffset() : 0;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return new LocalSearchScope(getContainingFile());
    }

    @Override
    public PsiElement getNameIdentifier() {
        return getNameElement();
    }

    @Override
    public String toString() {
        PsiElement nullableId = findChildByType(PasTypes.NAMESPACE_NAME);
        return super.toString() + ":" + (nullableId == null? null : nullableId.getText());
    }

    @Override
    public ItemPresentation getPresentation() {
        return PascalParserUtil.getPresentation(this);
    }

}
