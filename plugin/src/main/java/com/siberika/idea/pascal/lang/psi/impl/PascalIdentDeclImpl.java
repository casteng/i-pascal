package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasClassPropertySpecifier;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasConstExpression;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasVarDeclaration;
import com.siberika.idea.pascal.lang.psi.PasVarValueSpec;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import com.siberika.idea.pascal.lang.stub.PasIdentStubElementType;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class PascalIdentDeclImpl extends PascalNamedStubElement<PasIdentStub> implements PascalIdentDecl {

    protected static final Logger LOG = Logger.getInstance(PascalIdentDeclImpl.class.getName());

    volatile private Pair<String, PasField.Kind> myCachedType;
    private List<String> subMembers;                            // members which can be qualified by this ident as well as accessed directly (enums)
    private ReentrantLock subMembersLock = new ReentrantLock();

    PascalIdentDeclImpl(ASTNode node) {
        super(node);
    }

    PascalIdentDeclImpl(PasIdentStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
        myCachedType = Pair.create(stub.getTypeString(), stub.getTypeKind());
        subMembers = stub.getSubMembers();
    }

    public static PascalIdentDecl create(PasIdentStub stub, PasIdentStubElementType elementType) {
        return new PasNamedIdentDeclImpl(stub, elementType);
    }

    @Override
    public void invalidateCache(boolean subtreeChanged) {
        super.invalidateCache(subtreeChanged);
        if (SyncUtil.lockOrCancel(subMembersLock)) {
            subMembers = null;
            subMembersLock.unlock();
        }
        myCachedType = null;
    }

    @Nullable
    @Override
    public PasIdentStub retrieveStub() {
        return getGreenStub();
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

    @NotNull
    @Override
    public PasField.Access getAccess() {
        PasIdentStub stub = retrieveStub();
        if (stub != null) {
            return stub.getAccess();
        }
        PasField.FieldType type = getType();
        if (type == PasField.FieldType.VARIABLE) {
            return PasField.Access.READWRITE;
        } else if (type == PasField.FieldType.CONSTANT) {
            return StringUtils.isBlank(getTypeString()) ? PasField.Access.READONLY : PasField.Access.READWRITE;
        } else if (type == PasField.FieldType.PROPERTY) {
            return getPropertyAccess();
        }
        return PasField.Access.READONLY;
    }

    @Nullable
    @Override
    public String getValue() {
        PasIdentStub stub = retrieveStub();
        if (stub != null) {
            return stub.getValue();
        }
        PsiElement parent = getParent();
        if (parent instanceof PasVarDeclaration) {
            PasVarValueSpec varSpec = ((PasVarDeclaration) parent).getVarValueSpec();
            return varSpec != null ? varSpec.getText() : null;
        } else if (parent instanceof PasConstDeclaration) {
            PasConstExpression expr = PsiTreeUtil.getChildOfType(parent, PasConstExpression.class);
            return expr != null ? expr.getText() : null;
        } else {
            return null;
        }
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
        PsiElement parent = getParent();
        if (parent instanceof PasEnumType) {
            parent = parent.getParent();
            if (parent instanceof PasTypeDecl && parent.getParent() instanceof PasTypeDeclaration) {
                PasGenericTypeIdent typeId = ((PasTypeDeclaration) parent.getParent()).getGenericTypeIdent();
                String parentName = typeId.getNamedIdentDecl().getName();
                if (!StringUtils.isEmpty(parentName)) {
                    return calcScopeUniqueName(scope) + "." + parentName + "." + PsiUtil.getFieldName(this);
                }
            }
        }
        return calcScopeUniqueName(scope) + "." + PsiUtil.getFieldName(this);
    }

    private void ensureTypeResolved() {
        helper.ensureCacheActual();
        if (myCachedType == null) {
            myCachedType = ResolveUtil.retrieveDeclarationType(this);
        }
    }

    private PasField.Access getPropertyAccess() {
        PasField.Access res = PasField.Access.READWRITE;
        PsiElement parent = getParent();
        if (parent instanceof PasClassProperty) {
            boolean read = false;
            boolean write = false;
            for (PasClassPropertySpecifier specifier : ((PasClassProperty) parent).getClassPropertySpecifierList()) {
                ASTNode node = specifier.getNode().getFirstChildNode();
                if (node.getElementType() == PasTypes.READ) {
                    read = true;
                } else if (node.getElementType() == PasTypes.WRITE) {
                    write = true;
                }
            }
            if (read) {
                res = write ? PasField.Access.READWRITE : PasField.Access.READONLY;
            } else {
                if (write) {
                    res = PasField.Access.WRITEONLY;
                } else {
                    // TODO: refer to parent
                }
            }
        } else {
            LOG.info(String.format("ERROR: parent is not PasClassProperty but %s", parent != null ? parent.getClass().getSimpleName() : "<null>"));
        }
        return res;
    }

    @Override
    public boolean isDefaultProperty() {
        PasIdentStub stub = retrieveStub();
        if (stub != null) {
            return stub.isDefaultProperty();
        }
        if (!helper.isDefaultPropertyInit()) {
            helper.setDefaultProperty(PsiUtil.isDefaultProperty(this));
        }
        return helper.isDefaultProperty();
    }

    protected void initAllFlags() {
        super.initAllFlags();
        isDefaultProperty();
    }

}
