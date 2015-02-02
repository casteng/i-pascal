package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.search.ProjectScopeImpl;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public abstract class PascalNamedElementImpl extends ASTWrapperPsiElement implements PascalNamedElement {
    private static final int MAX_SHORT_TEXT_LENGTH = 32;
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

    @Override
    public String getNamespace() {
        String name = getName();
        int pos = name.lastIndexOf(".");
        return pos >= 0 ? name.substring(0, pos) : null;
    }

    @Override
    public String getNamePart() {
        String name = getName();
        int pos = name.lastIndexOf(".");
        return pos >= 0 ? name.substring(pos + 1) : name;
    }

    @Nullable
    protected PsiElement getNameElement() {
        if ((this instanceof PasNamespaceIdent) || (this instanceof PascalQualifiedIdent)) {
            return this;
        }
        PsiElement result = findChildByType(PasTypes.NAMESPACE_IDENT);
        if (null == result) {
            result = findChildByType(PasTypes.IDENT_KW);
        }
        if (null == result) {
            result = findChildByType(PasTypes.NAME);
        }
        if (null == result) {
            PascalNamedElement namedChild = PsiTreeUtil.findChildOfType(this, PascalNamedElement.class);
            result = namedChild != null ? namedChild.getNameIdentifier() : null;
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
        return (element != null) && (element != this) ? element.getTextOffset() : getNode().getStartOffset();
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return new ProjectScopeImpl(getProject(), FileIndexFacade.getInstance(getProject()));
    }

    @Override
    public PsiElement getNameIdentifier() {
        return getNameElement();
    }

    @Override
    public String toString() {
        try {
            return "[" + getClass().getSimpleName() + "]\"" + getName() + "\" ^" + getParent() + "..." + getShortText(getParent());
        } catch (NullPointerException e) {
            return "<NPE>";
        }
    }

    private static String getShortText(PsiElement parent) {
        if (null == parent) { return ""; }
        int lfPos = parent.getText().indexOf("\n");
        if (lfPos > 0) {
            return parent.getText().substring(0, lfPos);
        } else {
            return parent.getText().substring(0, Math.min(parent.getText().length(), MAX_SHORT_TEXT_LENGTH));
        }
    }

    @Override
    public ItemPresentation getPresentation() {
        return PascalParserUtil.getPresentation(this);
    }

    @Override
    public PsiReference getReference() {
        PsiReference[] refs = getReferences();
        return refs.length > 0 ? refs[0] : null;
    }

    @Override
    @NotNull
    public PsiReference[] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PascalNamedElementImpl that = (PascalNamedElementImpl) o;

        if (!getName().equals(that.getName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
