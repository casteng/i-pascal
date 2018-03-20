package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasDeclSection;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.stub.PasExportedRoutineStub;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class PascalExportedRoutineImpl extends PasStubScopeImpl<PasExportedRoutineStub> implements PasDeclSection, PascalExportedRoutine {

    protected static final Logger LOG = Logger.getInstance(PascalExportedRoutineImpl.class);

    private PasTypeID typeId;

    public PascalExportedRoutineImpl(ASTNode node) {
        super(node);
    }

    public PascalExportedRoutineImpl(PasExportedRoutineStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    protected String calcUniqueName() {
        PasEntityScope scope = getContainingScope();
        return (scope != null ? scope.getUniqueName() + "." : "") + PsiUtil.getFieldName(this);
    }

    @Override
    protected String calcKey() {
        return RoutineUtil.calcKey(this);       //TODO: remove
    }

    @Nullable
    @Override
    public PasField getField(String name) {
        return null;                               // No fields within exported routine
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        return Collections.emptyList();            // No fields within exported routine
    }

    @Override
    public String getCanonicalName() {
        PasExportedRoutineStub stub = getStub();
        if (stub != null) {
            return stub.getCanonicalName();
        }
        return PsiUtil.normalizeRoutineName(this);
    }

    @Override
    public PasField.Visibility getVisibility() {
        return PasField.Visibility.PUBLIC;         // TODO: implement
    }

    public boolean isConstructor() {
        PasExportedRoutineStub stub = getStub();
        if (stub != null) {
            return stub.isConstructor();
        }
        return RoutineUtil.isConstructor(this);
    }

    public boolean isFunction() {
        PasExportedRoutineStub stub = getStub();
        if (stub != null) {
            return stub.isFunction();
        }
        return findChildByFilter(RoutineUtil.FUNCTION_KEYWORDS) != null;
    }

    @NotNull
    public String getFunctionTypeStr() {
        PasExportedRoutineStub stub = getStub();
        if (stub != null) {
            return stub.getFunctionTypeStr();
        }
        return resolveFunctionTypeStr();
    }

    @Override
    public PasTypeID getFunctionTypeIdent() {
        if (null == typeId) {
            typeId = PsiTreeUtil.findChildOfType(findChildByClass(PasTypeDecl.class), PasTypeID.class);
        }
        return typeId;
    }

    private String resolveFunctionTypeStr() {
        if (isConstructor()) {                                      // Return struct type name
            PasEntityScope scope = getContainingScope();
            return scope != null ? scope.getName() : "";
        }
        PasTypeDecl type = findChildByClass(PasTypeDecl.class);
        PasTypeID typeId = PsiTreeUtil.findChildOfType(type, PasTypeID.class);
        if (typeId != null) {
            return typeId.getFullyQualifiedIdent().getName();
        }
        return type != null ? type.getText() : "";
    }

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
        List<PasEntityScope> res = calcParentScopesStub();
        if (res != null) {
            return PsiUtil.packSmartPointers(res);
        }
        return Collections.emptyList();
    }

    private List<PasEntityScope> calcParentScopesStub() {
        PasExportedRoutineStub stub = getStub();
        if (stub != null) {
            StubElement parentStub = stub.getParentStub();
            PsiElement parEl = parentStub != null ? parentStub.getPsi() : null;
            if (parEl instanceof PasEntityScope) {
                return Collections.singletonList((PasEntityScope) parEl);
            }
        }
        return null;
    }

}
