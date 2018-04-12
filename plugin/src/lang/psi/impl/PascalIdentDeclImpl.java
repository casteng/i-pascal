package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
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
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class PascalIdentDeclImpl extends PascalNamedStubElement<PasIdentStub> implements PascalIdentDecl {

    private Pair<String, PasField.Kind> myCachedType;
    private List<String> subMembers;                            // members which can be qualified by this ident as well as accessed directly (enums)
    private ReentrantLock typeLock = new ReentrantLock();
    private ReentrantLock subMembersLock = new ReentrantLock();

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
        Pair<String, PasField.Kind> cachedType = ensureTypeResolved();
        return cachedType != null ? cachedType.first : null;
    }

    @Nullable
    @Override
    public PasField.Kind getTypeKind() {
        PasIdentStub stub = retrieveStub();
        if (stub != null) {
            return stub.getTypeKind();
        }
        Pair<String, PasField.Kind> type = ensureTypeResolved();
        return type != null ? type.second : null;
    }

    private Pair<String, PasField.Kind> ensureTypeResolved() {
        if (SyncUtil.lockOrCancel(typeLock)) {
            try {
                if (myCachedType == null) {
                    myCachedType = ResolveUtil.retrieveDeclarationType(this);
                    return myCachedType;
                }
            } finally {
                typeLock.unlock();
            }
        }
        return myCachedType;
    }

    @NotNull
    public List<String> getSubMembers() {
        PasIdentStub stub = retrieveStub();
        if (stub != null) {
            return stub.getSubMembers();
        }
        if (SyncUtil.lockOrCancel(subMembersLock)) {
            try {
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
            } finally {
                subMembersLock.unlock();
            }
        }
        return subMembers;
    }

    protected String calcUniqueName() {
        PasEntityScope scope = PsiUtil.getNearestAffectingScope(this);
        return (scope != null ? scope.getUniqueName() + "." : "") + PsiUtil.getFieldName(this);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        if (SyncUtil.lockOrCancel(subMembersLock)) {
            subMembers = null;
            subMembersLock.unlock();
        }
        if (SyncUtil.lockOrCancel(typeLock)) {
            myCachedType = null;
            typeLock.unlock();
        }
    }

}
