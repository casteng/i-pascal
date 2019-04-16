package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Author: George Bakhtadze
 * Date: 06/09/2013
 */
public abstract class PascalRoutineImpl extends PasScopeImpl implements PascalRoutine, PasDeclSection, HasTypeParameters {

    private static final Cache<String, PascalHelperScope.Members> cache = CacheBuilder.newBuilder().softValues().build();

    volatile private List<String> typeParameters;
    volatile private Collection<PasWithStatement> withStatements;

    private final Callable<? extends PascalHelperScope.Members> MEMBER_BUILDER = this.new MemberBuilder();

    PascalRoutineImpl(ASTNode node) {
        super(node);
    }

    @Nullable
    public abstract PasFormalParameterSection getFormalParameterSection();

    @Override
    protected PascalHelperNamed createHelper() {
        return new PascalHelperRoutine(this);
    }

    @Override
    public void invalidateCache(boolean subtreeChanged) {
        typeParameters = null;
        withStatements = null;
    }

    @NotNull
    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.ROUTINE;
    }

    @NotNull
    @Override
    public List<String> getTypeParameters() {
        if (null == typeParameters) {
            PsiElement nameIdent = getNameIdentifier();
            List<PasGenericPostfix> postfixes = nameIdent instanceof PasClassQualifiedIdent ? ((PasClassQualifiedIdent) nameIdent).getGenericPostfixList() : Collections.emptyList();
            typeParameters = postfixes.isEmpty() ? Collections.emptyList() : RoutineUtil.parseTypeParametersStr(postfixes.get(0).getText());
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
    private PascalHelperScope.Members getMembers(Cache<String, PascalHelperScope.Members> cache, Callable<? extends PascalHelperScope.Members> builder) {
        ensureChache(cache);
        try {
            PascalHelperScope.Members res = cache.get(getKey(), builder);
            if (!res.isCachable()) {
                cache.invalidate(getKey());
            }
            return res;
        } catch (Exception e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.warn("Error occured during building members for: " + this, e.getCause());
                invalidateCaches(getKey());
                return PascalHelperScope.EMPTY_MEMBERS;
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

    @Nullable
    @Override
    public PasField getField(String name) {
        return getMembers(cache, MEMBER_BUILDER).all.get(name.toUpperCase());
    }

    @Nullable
    @Override
    public PascalRoutine getRoutine(String reducedName) {
        return RoutineUtil.findRoutine(getAllFields(), reducedName);
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        return getMembers(cache, MEMBER_BUILDER).all.values();
    }

    static void invalidate(String key) {
        cache.invalidate(key);
    }

    private class MemberBuilder implements Callable<PascalHelperScope.Members> {

        @Override
        public PascalHelperScope.Members call() {
            if (null == getContainingFile()) {
                PascalPsiImplUtil.logNullContainingFile(PascalRoutineImpl.this);
                return null;
            }
            if (building) {
                LOG.info("WARNING: Reentered in routine.buildXXX");
                return PascalHelperScope.Members.createNotCacheable();
//                throw new ProcessCanceledException();
            }
            building = true;
            try {
                PascalHelperScope.Members res = new PascalHelperScope.Members();
                res.stamp = getStamp(getContainingFile());

                collectFormalParameters(res);
                PascalHelperScope.collectFields(PascalRoutineImpl.this, PascalRoutineImpl.this, PasField.Visibility.STRICT_PRIVATE, res.all, res.redeclared);

                addSelf(res);

                LOG.debug(PsiUtil.getFieldName(PascalRoutineImpl.this) + ": buildMembers: " + res.all.size() + " members");
//                    System.out.println(PsiUtil.getFieldName(PascalRoutineImpl.this) + ": buildMembers: " + res.all.size() + " members");
                return res;
            } finally {
                building = false;
            }
        }
    }

    private void collectFormalParameters(PascalHelperScope.Members res) {
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
        if (routine.isFunction() && !res.all.containsKey(BUILTIN_RESULT_UPPER)) {
            res.all.put(BUILTIN_RESULT_UPPER.toUpperCase(), new PasField(this, routine, BUILTIN_RESULT, PasField.FieldType.PSEUDO_VARIABLE, PasField.Visibility.STRICT_PRIVATE));
        }
    }

    @NotNull
    @Override
    public List<String> getFormalParameterNames() {
        getHelper().calcFormalParameters();
        return getHelper().formalParameterNames;
    }

    @NotNull
    @Override
    public List<String> getFormalParameterTypes() {
        getHelper().calcFormalParameters();
        return getHelper().formalParameterTypes;
    }

    @NotNull
    @Override
    public List<ParamModifier> getFormalParameterAccess() {
        getHelper().calcFormalParameters();
        return getHelper().formalParameterAccess;
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
        return getHelper().calcFunctionTypeStr();
    }

    @Nullable
    public PasTypeID getFunctionTypeIdent() {
        return PsiTreeUtil.findChildOfType(findChildByClass(PasTypeDecl.class), PasTypeID.class);
    }

    private void addSelf(PascalHelperScope.Members res) {
        PasEntityScope scope = getContainingScope();
        if ((scope != null) && (scope.getParent() instanceof PasTypeDecl)) {
            PasField field = new PasField(this, scope, BUILTIN_SELF, PasField.FieldType.PSEUDO_VARIABLE, PasField.Visibility.STRICT_PRIVATE);
            PasTypeDecl typeDecl = (PasTypeDecl) scope.getParent();
            field.setValueType(new PasField.ValueType(field, PasField.Kind.STRUCT, null, typeDecl));
            res.all.put(BUILTIN_SELF_UPPER, field);
        }
    }

    private void addField(PascalHelperScope.Members res, PascalNamedElement element, PasField.FieldType fieldType) {
        PasField field = new PasField(this, element, element.getName(), fieldType, PasField.Visibility.STRICT_PRIVATE);
        res.all.put(field.name.toUpperCase(), field);
    }

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
        PasEntityScope scope = getContainingScope();
        return scope != null ? Collections.singletonList(PsiUtil.createSmartPointer(scope)) : Collections.emptyList();
    }

    @NotNull
    @Override
    public Collection<PasWithStatement> getWithStatements() {
        if (null == withStatements) {
            withStatements = PsiTreeUtil.findChildrenOfType(this, PasWithStatement.class);
        }
        return withStatements;
    }

    private PascalHelperRoutine getHelper() {
        return (PascalHelperRoutine) helper;
    }

}
