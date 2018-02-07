package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
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
public abstract class PascalRoutineImpl<T extends StubElement> extends PasStubScopeImpl<T> implements PasEntityScope, PasDeclSection {

    private static final Cache<String, Members> cache = CacheBuilder.newBuilder().softValues().build();

    private static final TokenSet FUNCTION_KEYWORDS = TokenSet.create(PasTypes.FUNCTION, PasTypes.OPERATOR);

    private ReentrantLock parentLock = new ReentrantLock();
    private boolean parentBuilding = false;

    private final Callable<? extends Members> MEMBER_BUILDER = this.new MemberBuilder();

    @Nullable
    public abstract PasFormalParameterSection getFormalParameterSection();

    public PascalRoutineImpl(ASTNode node) {
        super(node);
    }

    public PascalRoutineImpl(T stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    protected String calcKey() {
        StringBuilder sb = new StringBuilder(PsiUtil.getFieldName(this));
        sb.append(PsiUtil.isForwardProc(this) ? "-fwd" : "");
        if (this instanceof PasExportedRoutine) {
            sb.append("^intf");
        } else {
            sb.append("^impl");
        }

        PasEntityScope scope = this.getContainingScope();
        sb.append(scope != null ? "." + scope.getKey() : "");

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
        PascalRoutineImpl routine = this;
        List<PasNamedIdent> params = PsiUtil.getFormalParameters(getFormalParameterSection());
        if (params.isEmpty() && (this instanceof PasRoutineImplDecl)) {         // If this is implementation with formal parameters omitted take formal parameters from routine declaration
            PsiElement decl = SectionToggle.retrieveDeclaration(this, true);
            if (decl instanceof PascalRoutineImpl) {
                routine = (PascalRoutineImpl) decl;
                params = PsiUtil.getFormalParameters(routine.getFormalParameterSection());
            }
        }
        for (PasNamedIdent parameter : params) {
            addField(res, parameter, PasField.FieldType.VARIABLE);
        }
        if (routine.isFunction() && !res.all.containsKey(BUILTIN_RESULT.toUpperCase())) {
            res.all.put(BUILTIN_RESULT.toUpperCase(), new PasField(this, routine, BUILTIN_RESULT, PasField.FieldType.PSEUDO_VARIABLE, PasField.Visibility.STRICT_PRIVATE));
        }
    }

    private boolean isFunction() {
        return findChildByFilter(FUNCTION_KEYWORDS) != null;
    }

    private void addSelf(Members res) {
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
    public String getFunctionTypeStr() {
        if (isConstructor()) {
            if (this instanceof PasExportedRoutine) {
                PasEntityScope scope = getContainingScope();
                return scope != null ? scope.getName() : "";
            } else {
                String ns = getNamespace();
                return ns.substring(ns.lastIndexOf('.') + 1);
            }
        }
        PasTypeDecl type = findChildByClass(PasTypeDecl.class);
        PasTypeID typeId = PsiTreeUtil.findChildOfType(type, PasTypeID.class);
        if (typeId != null) {
            return typeId.getFullyQualifiedIdent().getName();
        }
        return type != null ? type.getText() : "";
    }

    public boolean isConstructor() {
        return getFirstChild().getNode().getElementType() == PasTypes.CONSTRUCTOR;
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
                LOG.warn("Error occured during building parents for: " + this, e);
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



// Copied from PascalNamedElementImpl as we can't extend that class. TODO: Move to another place

    private volatile String myCachedName;

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myCachedName = null;
    }

    @NotNull
    @Override
    synchronized public String getName() {
        if ((myCachedName == null) || (myCachedName.length() == 0)) {
            myCachedName = PascalNamedElementImpl.calcName(getNameElement());
        }
        return myCachedName;
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
        return null;
    }

    @Nullable
    private PsiElement getNameElement() {
        if ((this instanceof PasNamespaceIdent) || (this instanceof PascalQualifiedIdent)) {
            return this;
        }
        PsiElement result = findChildByType(PasTypes.NAMESPACE_IDENT);
        if (null == result) {
            PascalNamedElement namedChild = PsiTreeUtil.getChildOfType(this, PascalNamedElement.class);
            result = namedChild != null ? namedChild.getNameIdentifier() : null;
        }
        if (null == result) {
            result = findChildByType(NAME_TYPE_SET);
        }
        return result;
    }

}
