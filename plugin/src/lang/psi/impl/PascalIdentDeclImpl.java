package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import com.siberika.idea.pascal.lang.stub.PasIdentStubElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PascalIdentDeclImpl extends StubBasedPsiElementBase<PasIdentStub> implements PascalIdentDecl {

    public PascalIdentDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalIdentDeclImpl(PasIdentStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
        myCachedType = Pair.create(stub.getTypeString(), stub.getTypeKind());
    }

    public static PascalIdentDecl create(PasIdentStub stub, PasIdentStubElementType elementType) {
        return new PasNamedIdentDeclImpl(stub, elementType);
    }

    private volatile Pair<String, PasField.Kind> myCachedType;

    @Nullable
    @Override
    public PasIdentStub getStub() {      //===*** TODO: remove
        PasIdentStub stub = super.getStub();
        return stub != null ? stub : getGreenStub();
    }

    @Nullable
    @Override
    public String getTypeString() {
        PasIdentStub stub = getStub();
        if (stub != null) {
            return stub.getTypeString();
        }
        ensureTypeResolved();
        return myCachedType != null ? myCachedType.first : null;
    }

    @Nullable
    @Override
    public PasField.Kind getTypeKind() {
        PasIdentStub stub = getStub();
        if (stub != null) {
            return stub.getTypeKind();
        }
        ensureTypeResolved();
        return myCachedType != null ? myCachedType.second : null;
    }

    synchronized private void ensureTypeResolved() {
        if (myCachedType == null) {
            myCachedType = ResolveUtil.getDeclarationType(this);
        }
    }

    // From PascalNamedElementImpl
    private String myCachedName;

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myCachedName = null;
    }

    @NotNull
    @Override
    public String getName() {
        PasIdentStub stub = getStub();
        if (stub != null) {
            return stub.getName();
        }
        synchronized (this) {
            if ((myCachedName == null) || (myCachedName.length() == 0)) {
                myCachedName = PascalNamedElementImpl.calcName(getNameElement());
            }
            return myCachedName;
        }
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
