package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PasDeclSection;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.stub.PasExportedRoutineStub;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public abstract class PascalExportedRoutineImpl extends PasStubScopeImpl<PasExportedRoutineStub> implements PasDeclSection, PascalExportedRoutine {

    protected static final Logger LOG = Logger.getInstance(PascalExportedRoutineImpl.class);

    private PasTypeID typeId;
    private ReentrantLock typeIdLock = new ReentrantLock();
    private List<String> formalParameterNames;
    private List<PasField.Access> formalParameterAccess;
    private ReentrantLock parametersLock = new ReentrantLock();

    public PascalExportedRoutineImpl(ASTNode node) {
        super(node);
    }

    public PascalExportedRoutineImpl(PasExportedRoutineStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @NotNull
    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.ROUTINE;
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
        PasExportedRoutineStub stub = retrieveStub();
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
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.isConstructor();
        }
        return RoutineUtil.isConstructor(this);
    }

    public boolean isFunction() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.isFunction();
        }
        return findChildByFilter(RoutineUtil.FUNCTION_KEYWORDS) != null;
    }

    @Override
    public boolean hasParameters() {
        return !getFormalParameterNames().isEmpty();
    }

    @NotNull
    public String getFunctionTypeStr() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.getFunctionTypeStr();
        }
        return resolveFunctionTypeStr();
    }

    @Override
    public PasTypeID getFunctionTypeIdent() {
        if (SyncUtil.lockOrCancel(typeIdLock)) {
            try {
                if (null == typeId) {
                    typeId = PsiTreeUtil.findChildOfType(findChildByClass(PasTypeDecl.class), PasTypeID.class);
                }
            } finally {
                typeIdLock.unlock();
            }
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
        return Collections.singletonList(SmartPointerManager.createPointer(Objects.requireNonNull(getContainingScope())));
    }

    @Override
    void calcContainingScope() {
        PasEntityScope scope = PsiTreeUtil.getParentOfType(this, PasEntityScope.class);
        if (scope != null) {
            containingScope = PsiUtil.createSmartPointer(scope);
        } else {
            LOG.info("ERROR: containing scope not found for: " + getName());
        }
    }

    @NotNull
    @Override
    public List<String> getFormalParameterNames() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.getFormalParameterNames();
        }
        calcFormalParameters();
        return formalParameterNames;
    }

    @NotNull
    @Override
    public List<PasField.Access> getFormalParameterAccess() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.getFormalParameterAccess();
        }
        calcFormalParameters();
        return formalParameterAccess;
    }

    private void calcFormalParameters() {
        if (SyncUtil.lockOrCancel(parametersLock)) {
            try {
                if (null == formalParameterNames) {
                    formalParameterNames = new SmartList<>();
                    formalParameterAccess = new SmartList<>();
                    RoutineUtil.calcFormalParameterNames(getFormalParameterSection(), formalParameterNames, formalParameterAccess);
                }
            } finally {
                parametersLock.unlock();
            }
        }
    }

    @Override
    public void invalidateCaches() {
        super.invalidateCaches();
        if (SyncUtil.lockOrCancel(parametersLock)) {
            formalParameterNames = null;
            parametersLock.unlock();
        }
        typeId = null;
    }
}
