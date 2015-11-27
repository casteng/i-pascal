package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasUnitImplementation;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
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
public abstract class PascalRoutineImpl extends PasScopeImpl implements PasEntityScope, PasDeclSection {
    private static final String BUILTIN_RESULT = "Result";
    private static final String BUILTIN_SELF = "Self";

    private static final Cache<String, Members> cache = CacheBuilder.newBuilder().build();

    private ReentrantLock parentLock = new ReentrantLock();
    private boolean parentBuilding = false;

    @Nullable
    public abstract PasFormalParameterSection getFormalParameterSection();

    public PascalRoutineImpl(ASTNode node) {
        super(node);
    }

    @Override
    protected String calcKey() {
        StringBuilder sb = new StringBuilder(PsiUtil.isForwardProc(this) ? "forward:" : "");
        PasEntityScope scope = this;
        while (scope != null) {
            sb.append(".").append(PsiUtil.getFieldName(scope));
            PsiElement section = PsiUtil.getNearestSection(scope);
            if (section instanceof PasUnitInterface) {
                sb.append(".interface");
            } else if (section instanceof PasUnitImplementation) {
                sb.append(".implementation");
            }
            scope = scope.getContainingScope();
        }
        //sb.append(".").append(getContainingFile() != null ? getContainingFile().getName() : "");
        //System.out.println(String.format("%s for %s", sb.toString(), this));
        return sb.toString();
    }

    @NotNull
    private Members getMembers(Cache<String, Members> cache, Callable<? extends Members> builder) {
        ensureChache(cache);
        try {
            return cache.get(getKey(), builder);
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

    @Nullable
    @Override
    public PasField getField(String name) {
        return getMembers(cache, this.new MemberBuilder()).all.get(name.toUpperCase());
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        return getMembers(cache, this.new MemberBuilder()).all.values();
    }

    public static void invalidate(String key) {
        cache.invalidate(key);
        parentCache.invalidate(key);
    }

    private class MemberBuilder implements Callable<Members> {
        @Override
        public Members call() throws Exception {
            if (null == getContainingFile()) {
                PascalPsiImplUtil.logNullContainingFile(PascalRoutineImpl.this);
                return null;
            }
            if (building) {
                LOG.info("WARNING: Reentered in buildXXX");
                return null;
            }
            building = true;
            Members res = new Members();
            res.stamp = getStamp(getContainingFile());

            List<PasNamedIdent> params = PsiUtil.getFormalParameters(getFormalParameterSection());
            for (PasNamedIdent parameter : params) {
                addField(res, parameter, PasField.FieldType.VARIABLE);
            }

            collectFields(PascalRoutineImpl.this, PasField.Visibility.STRICT_PRIVATE, res.all, res.redeclared);

            addPseudoFields(res);

            LOG.info(getName() + ": buildMembers: " + res.all.size() + " members");
            building = false;
            return res;
        }
    }

    private void addPseudoFields(Members res) {
        if (!res.all.containsKey(BUILTIN_RESULT.toUpperCase())) {
            res.all.put(BUILTIN_RESULT.toUpperCase(), new PasField(this, this, BUILTIN_RESULT, PasField.FieldType.PSEUDO_VARIABLE, PasField.Visibility.STRICT_PRIVATE));
        }

        PasEntityScope scope = getContainingScope();
        if ((scope != null) && (scope.getParent() instanceof PasTypeDecl)) {
            PasField field = new PasField(this, scope, BUILTIN_SELF, PasField.FieldType.PSEUDO_VARIABLE, PasField.Visibility.STRICT_PRIVATE);
            PasTypeDecl typeDecl =  (PasTypeDecl) scope.getParent();
            field.setValueType(new PasField.ValueType(field, PasField.Kind.STRUCT, null, typeDecl));
            res.all.put(BUILTIN_SELF.toUpperCase(), field);
        }
    }

    private void addField(Members res, PascalNamedElement element, PasField.FieldType fieldType) {
        PasField field = new PasField(this, element, element.getName(), fieldType, PasField.Visibility.STRICT_PRIVATE);
        res.all.put(field.name.toUpperCase(), field);
    }

    @Nullable
    public PasTypeID getFunctionTypeIdent() {
        PasTypeDecl type = findChildByClass(PasTypeDecl.class);
        return PsiTreeUtil.findChildOfType(type, PasTypeID.class);
    }

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
        if (!SyncUtil.tryLockQuiet(parentLock, SyncUtil.LOCK_TIMEOUT_MS)) {
            return Collections.emptyList();
        }
        try {
            if (parentBuilding) {
                return Collections.emptyList();
            }
            parentBuilding = true;
            ensureChache(parentCache);
            return parentCache.get(getKey(), new ParentBuilder()).scopes;
        } catch (Exception e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.warn("Error occured during building parents for: " + this, e.getCause());
                invalidateCaches(getKey());
                return Collections.emptyList();
            }
        } finally {
            parentBuilding = false;
            parentLock.unlock();
        }
    }

    private class ParentBuilder implements Callable<Parents> {
        @Override
        public Parents call() throws Exception {
            if (null == getContainingFile()) {
                PascalPsiImplUtil.logNullContainingFile(PascalRoutineImpl.this);
                return null;
            }

            Parents res = new Parents();
            res.stamp = getStamp(getContainingFile());

            PasClassQualifiedIdent ident = PsiTreeUtil.getChildOfType(PascalRoutineImpl.this, PasClassQualifiedIdent.class);
            if ((ident != null) && (ident.getSubIdentList().size() > 1)) {          // Should contain at least class name and method name parts
                NamespaceRec fqn = NamespaceRec.fromElement(ident.getSubIdentList().get(ident.getSubIdentList().size() - 2));
                res.scopes = Collections.emptyList();                             // To prevent infinite recursion
                PasEntityScope type = PasReferenceUtil.resolveTypeScope(fqn, true);
                if (type != null) {
                    res.scopes = Collections.singletonList(SmartPointerManager.getInstance(type.getProject()).createSmartPsiElementPointer(type));
                }
            } else {
                res.scopes = Collections.emptyList();
            }

            return res;
        }
    }

}
