package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import com.siberika.idea.pascal.lang.stub.PasIdentStubElementType;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PascalIdentDeclImpl extends PascalNamedStubElement<PasIdentStub> implements PascalIdentDecl {

    private Pair<String, PasField.Kind> myCachedType;
    private List<String> subMembers;                            // members which can be qualified by this ident as well as accessed directly (enums)

    public PascalIdentDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalIdentDeclImpl(PasIdentStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
        myCachedType = Pair.create(stub.getTypeString(), stub.getTypeKind());
        subMembers = stub.getSubMembers();
    }

    public static PascalIdentDecl create(PasIdentStub stub, PasIdentStubElementType elementType) {
        return new PasNamedIdentDeclImpl(stub, elementType);
    }

    @Nullable
    @Override
    public PasIdentStub retrieveStub() {
        PasIdentStub stub = super.getStub();
        return stub != null ? stub : getGreenStub();
    }

    @Nullable
    @Override
    public String getTypeString() {
        PasIdentStub stub = retrieveStub();
        if (stub != null) {
            return stub.getTypeString();
        }
        ensureTypeResolved();
        return myCachedType != null ? myCachedType.first : null;
    }

    @Nullable
    @Override
    public PasField.Kind getTypeKind() {
        PasIdentStub stub = retrieveStub();
        if (stub != null) {
            return stub.getTypeKind();
        }
        ensureTypeResolved();
        return myCachedType != null ? myCachedType.second : null;
    }

    synchronized private void ensureTypeResolved() {
        if (myCachedType == null) {
            myCachedType = ResolveUtil.retrieveDeclarationType(this);
        }
    }

    public List<String> getSubMembers() {
        PasIdentStub stub = retrieveStub();
        if (stub != null) {
            return stub.getSubMembers();
        }
        synchronized (this) {
            if (null == subMembers) {
                PasTypeDecl decl = getTypeKind() == PasField.Kind.ENUM ? PsiTreeUtil.getNextSiblingOfType(getParent(), PasTypeDecl.class) : null;
                PasEnumType enumDecl = decl != null ? PsiTreeUtil.findChildOfType(decl, PasEnumType.class) : null;
                if (enumDecl != null) {
                    List<PasNamedIdentDecl> constsList = enumDecl.getNamedIdentDeclList();
                    subMembers = new ArrayList<>(constsList.size());
                    for (PasNamedIdentDecl enumConstDecl : constsList) {
                        subMembers.add(enumConstDecl.getName());
                    }
                } else {
                    subMembers = Collections.emptyList();
                }
            }
        }
        return subMembers;
    }

    protected String calcUniqueName() {
        PasEntityScope scope = PsiUtil.getNearestAffectingScope(this);
        return (scope != null ? scope.getUniqueName() + "." : "") + PsiUtil.getFieldName(this);
    }

    // From PascalNamedElementImpl
    private String myCachedName;

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myCachedName = null;
        subMembers = null;
    }

    @NotNull
    @Override
    public String getName() {
        PasIdentStub stub = retrieveStub();
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

}
