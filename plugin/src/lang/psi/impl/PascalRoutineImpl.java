package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.HasTypeParameters;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasDeclSection;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasGenericPostfix;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: George Bakhtadze
 * Date: 06/09/2013
 */
public abstract class PascalRoutineImpl extends PasScopeImpl implements PascalRoutine, PasDeclSection, HasTypeParameters {

    private static final Cache<String, Members> cache = CacheBuilder.newBuilder().softValues().build();

    private ReentrantLock parentLock = new ReentrantLock();
    private List<SmartPsiElementPointer<PasEntityScope>> parentScopes;
    private List<String> formalParameterNames;
    private List<String> formalParameterTypes;
    private List<ParamModifier> formalParameterAccess;
    private String canonicalName;
    private ReentrantLock parametersLock = new ReentrantLock();
    private List<String> typeParameters;
    private ReentrantLock typeParametersLock = new ReentrantLock();

    private final Callable<? extends Members> MEMBER_BUILDER = this.new MemberBuilder();
    volatile private Collection<PasWithStatement> withStatements;

    @Nullable
    public abstract PasFormalParameterSection getFormalParameterSection();

    PascalRoutineImpl(ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.ROUTINE;
    }

    @NotNull
    @Override
    public List<String> getTypeParameters() {
        if (SyncUtil.lockOrCancel(typeParametersLock)) {
            try {
                if (null == typeParameters) {
                    PsiElement nameIdent = getNameIdentifier();
                    List<PasGenericPostfix> postfixes = nameIdent instanceof PasClassQualifiedIdent ? ((PasClassQualifiedIdent) nameIdent).getGenericPostfixList() : Collections.emptyList();
                    typeParameters = postfixes.isEmpty() ? Collections.emptyList() : RoutineUtil.parseTypeParametersStr(postfixes.get(0).getText());
                }
            } finally {
                typeParametersLock.unlock();
            }
        }
        return typeParameters;
    }

    @Override
    protected String calcKey() {
        StringBuilder sb = new StringBuilder(PsiUtil.getFieldName(this));
        if (PsiUtil.isForwardProc(this)) {
            sb.append("(fwd)");
        }
        sb.append("(impl)");

        PasEntityScope scope = this.getContainingScope();
        if (scope != null) {
            sb.append(".").append(scope.getKey());
        }

//        System.out.println(String.format("%s:%d - %s", PsiUtil.getFieldName(this), this.getTextOffset(), sb.toString()));
        return sb.toString();
    }

    @NotNull
    private Members getMembers(Cache<String, Members> cache, Callable<? extends Members> builder) {
        ensureChache(cache);
        try {
            Members res = cache.get(getKey(), builder);
            if (!res.isChachable()) {
                cache.invalidate(getKey());
            }
            return res;
        } catch (Exception e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.warn("Error occured during building members for: " + this, e.getCause());
                invalidateCaches(getKey());
                return EMPTY_MEMBERS;
            }
        }
    }

    @Override
    protected String calcUniqueName() {
        PasEntityScope scope = getContainingScope();
        return (scope != null ? scope.getUniqueName() + "." : "") + getCanonicalName();
    }

    @Override
    public String getCanonicalName() {
        SyncUtil.doWithLock(parametersLock, () -> {
            if (null == canonicalName) {
                canonicalName = RoutineUtil.calcCanonicalName(getName(), getFormalParameterTypes(), getFormalParameterAccess(), getFunctionTypeStr());
            }
        });
        return canonicalName;
    }

    @Override
    public PasField.Visibility getVisibility() {
        return PasField.Visibility.PUBLIC;         // TODO: implement
    }

    @Nullable
    @Override
    public PasField getField(String name) {
        return getMembers(cache, MEMBER_BUILDER).all.get(name.toUpperCase());
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        return getMembers(cache, MEMBER_BUILDER).all.values();
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        invalidateCaches();
    }

    @Override
    public void invalidateCaches() {
        if (SyncUtil.lockOrCancel(parentLock)) {
            parentScopes = null;
            parentLock.unlock();
        }
        if (SyncUtil.lockOrCancel(parametersLock)) {
            formalParameterNames = null;
            formalParameterTypes = null;
            formalParameterAccess = null;
            canonicalName = null;
            parametersLock.unlock();
        }
        if (cachedKey != null) {
            invalidate(cachedKey);
        }
    }

    static void invalidate(String key) {
        cache.invalidate(key);
    }

    private class MemberBuilder implements Callable<Members> {
        @Override
        public Members call() {
            if (null == getContainingFile()) {
                PascalPsiImplUtil.logNullContainingFile(PascalRoutineImpl.this);
                return null;
            }
            if (building) {
                LOG.info("WARNING: Reentered in routine.buildXXX");
                return Members.createNotCacheable();
//                throw new ProcessCanceledException();
            }
            building = true;
            try {
                Members res = new Members();
                res.stamp = getStamp(getContainingFile());

                collectFormalParameters(res);
                collectFields(PascalRoutineImpl.this, PasField.Visibility.STRICT_PRIVATE, res.all, res.redeclared);

                addSelf(res);

                LOG.debug(PsiUtil.getFieldName(PascalRoutineImpl.this) + ": buildMembers: " + res.all.size() + " members");
//                    System.out.println(PsiUtil.getFieldName(PascalRoutineImpl.this) + ": buildMembers: " + res.all.size() + " members");
                return res;
            } finally {
                building = false;
            }
        }
    }

    private void collectFormalParameters(Members res) {
        PascalRoutine routine = this;
        List<PascalNamedElement> params = PsiUtil.getFormalParameters(getFormalParameterSection());
        if (params.isEmpty() && (this instanceof PasRoutineImplDecl)) {         // If this is implementation with formal parameters omitted take formal parameters from routine declaration
            PsiElement decl = SectionToggle.retrieveDeclaration(this, true);
            if (decl instanceof PascalRoutine) {
                routine = (PascalRoutine) decl;
                params = PsiUtil.getFormalParameters(routine.getFormalParameterSection());
            }
        }
        for (PascalNamedElement parameter : params) {
            addField(res, parameter, PasField.FieldType.VARIABLE);
        }
        if (routine.isFunction() && !res.all.containsKey(BUILTIN_RESULT.toUpperCase())) {
            res.all.put(BUILTIN_RESULT.toUpperCase(), new PasField(this, routine, BUILTIN_RESULT, PasField.FieldType.PSEUDO_VARIABLE, PasField.Visibility.STRICT_PRIVATE));
        }
    }

    @NotNull
    @Override
    public List<String> getFormalParameterNames() {
        calcFormalParameters();
        return formalParameterNames;
    }

    @NotNull
    @Override
    public List<String> getFormalParameterTypes() {
        calcFormalParameters();
        return formalParameterTypes;
    }

    @NotNull
    @Override
    public List<ParamModifier> getFormalParameterAccess() {
        calcFormalParameters();
        return formalParameterAccess;
    }

    private void calcFormalParameters() {
        SyncUtil.doWithLock(parametersLock, () -> {
            if (null == formalParameterNames) {
                formalParameterNames = new SmartList<>();
                formalParameterAccess = new SmartList<>();
                formalParameterTypes = new SmartList<>();
                RoutineUtil.calcFormalParameterNames(getFormalParameterSection(), formalParameterNames, formalParameterTypes, formalParameterAccess);
            }
        });
    }

    public boolean isConstructor() {
        return RoutineUtil.isConstructor(this);
    }

    public boolean isFunction() {
        return findChildByFilter(RoutineUtil.FUNCTION_KEYWORDS) != null;
    }

    @Override
    public boolean hasParameters() {
        return PsiUtil.hasParameters(this);
    }

    @NotNull
    public String getFunctionTypeStr() {
        if (isConstructor()) {                                 // Return namespace part of constructor implementation name as type name
            String ns = getNamespace();
            ns = ns.substring(ns.lastIndexOf('.') + 1);
            List<String> typeParams = getTypeParameters();
            if (!typeParams.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("<");
                for (String typeParam : typeParams) {
                    if (sb.length() > 1) {
                        sb.append(", ");
                    }
                    sb.append(typeParam);
                }
                sb.append(">");
                ns = ns + sb.toString();
            } 
            return ns;
        }
        PasTypeDecl type = findChildByClass(PasTypeDecl.class);
        PasTypeID typeId = PsiTreeUtil.findChildOfType(type, PasTypeID.class);
        if (typeId != null) {
            return typeId.getFullyQualifiedIdent().getName();
        }
        return type != null ? type.getText() : "";
    }

    @Nullable
    public PasTypeID getFunctionTypeIdent() {
        return PsiTreeUtil.findChildOfType(findChildByClass(PasTypeDecl.class), PasTypeID.class);
    }

    private void addSelf(Members res) {
        PasEntityScope scope = getContainingScope();
        if ((scope != null) && (scope.getParent() instanceof PasTypeDecl)) {
            PasField field = new PasField(this, scope, BUILTIN_SELF, PasField.FieldType.PSEUDO_VARIABLE, PasField.Visibility.STRICT_PRIVATE);
            PasTypeDecl typeDecl = (PasTypeDecl) scope.getParent();
            field.setValueType(new PasField.ValueType(field, PasField.Kind.STRUCT, null, typeDecl));
            res.all.put(BUILTIN_SELF.toUpperCase(), field);
        }
    }

    private void addField(Members res, PascalNamedElement element, PasField.FieldType fieldType) {
        PasField field = new PasField(this, element, element.getName(), fieldType, PasField.Visibility.STRICT_PRIVATE);
        res.all.put(field.name.toUpperCase(), field);
    }

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
            if (SyncUtil.tryLockQuiet(parentLock, SyncUtil.LOCK_TIMEOUT_MS)) {
                try {
                    if (null == parentScopes) {
                        calcParentScopes();
                    }
                } finally {
                    parentLock.unlock();
                }
            } else {
                LOG.info("ERROR: can't lock for calculate parent scope for: " + getName());
            }
        return parentScopes;
    }

    @NotNull
    @Override
    public Collection<PasWithStatement> getWithStatements() {
        if (null == withStatements) {
            withStatements = PsiTreeUtil.findChildrenOfType(this, PasWithStatement.class);
        }
        return withStatements;
    }

    private void calcParentScopes() {
        parentScopes = Collections.emptyList();                             // To prevent infinite recursion
        PasEntityScope scope = getContainingScope();
        if (scope != null) {
            parentScopes = Collections.singletonList(SmartPointerManager.getInstance(getProject()).createSmartPsiElementPointer(scope));
        }
    }

}
