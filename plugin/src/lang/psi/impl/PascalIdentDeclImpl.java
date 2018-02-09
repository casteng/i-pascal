package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.psi.PasEscapedIdent;
import com.siberika.idea.pascal.lang.psi.PasKeywordIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalIdentDeclImpl extends StubBasedPsiElementBase<PasIdentStub> implements PascalIdentDecl {

    public PascalIdentDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalIdentDeclImpl(PasIdentStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    private volatile String myCachedName;

    @Override
    @Nullable
    public PasEscapedIdent getEscapedIdent() {
        return findChildByClass(PasEscapedIdent.class);
    }

    @Override
    @Nullable
    public PasKeywordIdent getKeywordIdent() {
        return findChildByClass(PasKeywordIdent.class);
    }


    // From PascalNamedElementImpl
    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myCachedName = null;
    }

    @NotNull
    @Override
    synchronized public String getName() {
        if ((myCachedName == null) || (myCachedName.length() == 0)) {
            myCachedName = PascalNamedElementImpl.calcName(getNameElement());
        }
        return myCachedName;
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getNamePart() {
        return getName();
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return getNameElement();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        return null;                        //TODO: implement?
    }

    @Nullable
    private PsiElement getNameElement() {
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

}
