package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScopeImpl;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.PascalReference;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public abstract class PascalNamedElementImpl extends ASTWrapperPsiElement implements PascalNamedElement {
    private static final int MAX_SHORT_TEXT_LENGTH = 32;
    private static final TokenSet NAME_TYPE_SET = TokenSet.create(PasTypes.NAME, PasTypes.KEYWORD_IDENT, PasTypes.ESCAPED_IDENT);
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
    public String getName() {                                                      // TODO: synchronize?
        if ((myCachedName == null) || (myCachedName.length() == 0)) {
            myCachedName = calcName(getNameElement());
        }
        return myCachedName;
    }

    private static String calcName(PsiElement nameElement) {
        if ((nameElement != null) && (nameElement.getClass() == PasClassQualifiedIdentImpl.class)) {
            Iterator<PasSubIdent> it = ((PasClassQualifiedIdent) nameElement).getSubIdentList().iterator();
            StringBuilder sb = new StringBuilder(it.next().getName());
            while (it.hasNext()) {
                String name = it.next().getName();
                name = !name.startsWith("&") ? name : name.substring(1);
                sb.append(".").append(name);
            }
            return sb.toString();
        } else {
            if (nameElement != null) {
                String name = nameElement.getText();
                return !name.startsWith("&") ? name : name.substring(1);
            } else {
                return  "";
            }
        }
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
            PascalNamedElement namedChild = PsiTreeUtil.getChildOfType(this, PascalNamedElement.class);
            result = namedChild != null ? namedChild.getNameIdentifier() : null;
        }
        if (null == result) {
            result = findChildByType(NAME_TYPE_SET);
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
        if (PsiUtil.isLocalDeclaration(this)) {
            return GlobalSearchScope.fileScope(this.getContainingFile());
        }
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
        if ((this instanceof PasSubIdent) || (this instanceof PasRefNamedIdent) || (this.getParent() instanceof PascalRoutineImpl)) {
            if ((getNameElement() != null) && getTextRange().intersects(getNameElement().getTextRange())) {
                return new PsiReference[]{
                        new PascalReference(this, new TextRange(0, getName().length()))
                };
            }
        }
        return PsiReference.EMPTY_ARRAY;
    }

    @Override
    public boolean equals(Object o) {                                        // TODO: remove
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PascalNamedElementImpl that = (PascalNamedElementImpl) o;

        if (getParent() != that.getParent()) return false;
        if (!getName().equalsIgnoreCase(that.getName())) return false;
        if ((this instanceof PascalRoutineImpl) && (this != that)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getName().toUpperCase().hashCode();
        result = 31 * result + (getParent() != null ? getParent().hashCode() : 0);
        return result;
    }

}
