package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PasConstrainedTypeParam;
import com.siberika.idea.pascal.lang.psi.PasDeclSection;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.field.Flag;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.lang.stub.PasExportedRoutineStub;
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

    PascalExportedRoutineImpl(ASTNode node) {
        super(node);
    }

    PascalExportedRoutineImpl(PasExportedRoutineStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    public PascalHelperNamed createHelper() {
        return new PascalHelperRoutine(this);
    }

    @Override
    public void invalidateCache(boolean subtreeChanged) {
        super.invalidateCache(subtreeChanged);
        if (SyncUtil.lockOrCancel(typeIdLock)) {
            typeId = null;
            typeIdLock.unlock();
        }
    }

    private PascalHelperRoutine getHelper() {
        return (PascalHelperRoutine) helper;
    }

    @NotNull
    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.ROUTINE;
    }

    @Override
    protected boolean calcIsExported() {
        helper.ensureCacheActual();
        if (!helper.isFlagInit(Flag.EXPORTED)) {
            boolean tempExported;
            PsiElement parent = getParent();
            if (parent instanceof PascalStructType) {
                tempExported = ((PascalStructType) parent).isExported();
            } else {
                tempExported = parent instanceof PasUnitInterface;
            }
            helper.setFlag(Flag.EXPORTED, tempExported);
        }
        return helper.isFlagSet(Flag.EXPORTED);
    }

    @Override
    protected String calcKey() {
        return RoutineUtil.calcKey(this);       //TODO: remove
    }

    @Nullable
    @Override
    public PasField getField(String name) {
        for (PasField field : getAllFields()) {
            if (field.name.equalsIgnoreCase(name)) {
                return field;
            }
        }
        return null;                               // No fields but type parameters within exported routine
    }

    @Nullable
    @Override
    public PascalRoutine getRoutine(String reducedName) {
        return null;                               // No routines within exported routine
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {   // TODO: check stub
        return collectTypeParameters();            // No fields but type parameters within exported routine
    }

    @Override
    protected String calcUniqueName() {
        PasEntityScope scope = getContainingScope();
        return calcScopeUniqueName(scope) + "." + getCanonicalName();
    }

    @Override
    public String getCanonicalName() {
        return getHelper().getCanonicalName();
    }

    @Override
    public String getReducedName() {
        return getHelper().getReducedName();
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
        if (!helper.isFlagInit(Flag.CONSTRUCTOR)) {
            helper.setFlag(Flag.CONSTRUCTOR, RoutineUtil.isConstructor(this));
        }
        return helper.isFlagSet(Flag.CONSTRUCTOR);
    }

    public boolean isFunction() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.isFunction();
        }
        if (!helper.isFlagInit(Flag.FUNCTION)) {
            helper.setFlag(Flag.FUNCTION, findChildByFilter(RoutineUtil.FUNCTION_KEYWORDS) != null);
        }
        return helper.isFlagSet(Flag.FUNCTION);
    }

    @Override
    public boolean isOverloaded() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.isOverloaded();
        }
        if (!helper.isFlagInit(Flag.OVERLOADED)) {
            helper.setFlag(Flag.OVERLOADED, RoutineUtil.isOverloaded((PasExportedRoutine) this));
        }
        return helper.isFlagSet(Flag.OVERLOADED);
    }

    @Override
    public boolean isOverridden() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.isOverridden();
        }
        if (!helper.isFlagInit(Flag.OVERRIDDEN)) {
            helper.setFlag(Flag.OVERRIDDEN, RoutineUtil.isOverridden((PasExportedRoutine) this));
        }
        return helper.isFlagSet(Flag.OVERRIDDEN);
    }

    @Override
    public boolean isAbstract() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.isAbstract();
        }
        if (!helper.isFlagInit(Flag.ABSTRACT)) {
            helper.setFlag(Flag.ABSTRACT, RoutineUtil.isAbstract((PasExportedRoutine) this));
        }
        return helper.isFlagSet(Flag.ABSTRACT);
    }

    @Override
    public boolean isVirtual() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.isAbstract();
        }
        if (!helper.isFlagInit(Flag.VIRTUAL)) {
            helper.setFlag(Flag.VIRTUAL, RoutineUtil.isVirtual((PasExportedRoutine) this));
        }
        return helper.isFlagSet(Flag.VIRTUAL);
    }

    @Override
    public boolean isFinal() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.isAbstract();
        }
        if (!helper.isFlagInit(Flag.FINAL)) {
            helper.setFlag(Flag.FINAL, RoutineUtil.isFinal((PasExportedRoutine) this));
        }
        return helper.isFlagSet(Flag.FINAL);
    }

    protected void initAllFlags() {
        super.initAllFlags();
        isConstructor();
        isFunction();
        isOverloaded();
        isOverridden();
        isAbstract();
        isVirtual();
        isFinal();
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
        return getHelper().calcFunctionTypeStr();
    }

    @Override
    public PasTypeID getFunctionTypeIdent() {
        if (null == typeId) {
            PasTypeID newTypeId = PsiTreeUtil.findChildOfType(findChildByClass(PasTypeDecl.class), PasTypeID.class);
            if (SyncUtil.lockOrCancel(typeIdLock)) {
                typeId = newTypeId;
                typeIdLock.unlock();
            }
        }
        return typeId;
    }

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
        return Collections.singletonList(SmartPointerManager.getInstance(this.getProject()).createSmartPsiElementPointer(Objects.requireNonNull(getContainingScope())));
    }

    @NotNull
    @Override
    public List<String> getFormalParameterNames() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.getFormalParameterNames();
        }
        getHelper().calcFormalParameters();
        return getHelper().formalParameterNames;
    }

    @NotNull
    @Override
    public List<String> getFormalParameterTypes() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.getFormalParameterTypes();
        }
        getHelper().calcFormalParameters();
        return getHelper().formalParameterTypes;
    }

    @NotNull
    @Override
    public List<ParamModifier> getFormalParameterAccess() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.getFormalParameterAccess();
        }
        getHelper().calcFormalParameters();
        return getHelper().formalParameterAccess;
    }

    @NotNull
    @Override
    public List<String> getFormalParameterDefaultValues() {
        PasExportedRoutineStub stub = retrieveStub();
        if (stub != null) {
            return stub.getFormalParameterValues();
        }
        getHelper().calcFormalParameters();
        return getHelper().formalParameterDefaultValues;
    }

    @NotNull
    @Override
    public Collection<PasWithStatement> getWithStatements() {
        return Collections.emptyList();
    }

    private List<PasField> collectTypeParameters() {
        List<PasField> res = new SmartList<>();
        for (PasConstrainedTypeParam typeParam : getConstrainedTypeParamList()) {
            for (PasNamedIdent typeParamIdent : typeParam.getNamedIdentList()) {
                res.add(new PasField(this, typeParamIdent, typeParamIdent.getName(), PasField.FieldType.TYPE, PasField.Visibility.STRICT_PRIVATE));
            }
        }
        return res.isEmpty() ? Collections.emptyList() : res;
    }

}
