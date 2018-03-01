package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import com.siberika.idea.pascal.lang.stub.PasIdentStubElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PascalIdentDeclImpl extends PascalNamedStubElement<PasIdentStub> implements PascalIdentDecl {

    private Pair<String, PasField.Kind> myCachedType;

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

}
