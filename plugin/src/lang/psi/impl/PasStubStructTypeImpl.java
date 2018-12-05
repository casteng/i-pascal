package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasGenericDefinition;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import com.siberika.idea.pascal.lang.psi.PascalInterfaceDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.PascalVariableDeclaration;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.struct.PasStructStub;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

public abstract class PasStubStructTypeImpl<T extends PascalStructType, B extends PasStructStub<T>>
        extends PasStubScopeImpl<B> implements PascalStructType<B> {

    public static final Logger LOG = Logger.getInstance(PasStubStructTypeImpl.class.getName());

    private static final Cache<String, Members> cache = CacheBuilder.newBuilder().softValues().build();

    private static final Map<String, PasField.Visibility> STR_TO_VIS;

    private final Callable<? extends Members> MEMBER_BUILDER = this.new MemberBuilder();

    static {
        STR_TO_VIS = new HashMap<String, PasField.Visibility>(PasField.Visibility.values().length);
        STR_TO_VIS.put("INTERNAL", PasField.Visibility.INTERNAL);
        STR_TO_VIS.put("STRICTPRIVATE", PasField.Visibility.STRICT_PRIVATE);
        STR_TO_VIS.put("PRIVATE", PasField.Visibility.PRIVATE);
        STR_TO_VIS.put("STRICTPROTECTED", PasField.Visibility.STRICT_PROTECTED);
        STR_TO_VIS.put("PROTECTED", PasField.Visibility.PROTECTED);
        STR_TO_VIS.put("PUBLIC", PasField.Visibility.PUBLIC);
        STR_TO_VIS.put("PUBLISHED", PasField.Visibility.PUBLISHED);
        STR_TO_VIS.put("AUTOMATED", PasField.Visibility.AUTOMATED);
        assert STR_TO_VIS.size() == PasField.Visibility.values().length;
    }

    private List<String> parentNames = null;
    private List<SmartPsiElementPointer<PasEntityScope>> parentScopes;
    private ReentrantLock parentNamesLock = new ReentrantLock();
    private ReentrantLock parentScopesLock = new ReentrantLock();

    public PasStubStructTypeImpl(ASTNode node) {
        super(node);
    }

    public PasStubStructTypeImpl(final B stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @NotNull
    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.TYPE;
    }

    @Override
    protected boolean calcIsExported() {
        return false;
    }

    @NotNull
    @Override
    public List<String> getTypeParameters() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.getTypeParameters();
        }
        PsiElement nameElement = getNameElement();
        if (nameElement instanceof PasGenericTypeIdent) {
            PasGenericDefinition genericDefinition = ((PasGenericTypeIdent) nameElement).getGenericDefinition();
            return genericDefinition != null ? RoutineUtil.parseTypeParametersStr(genericDefinition.getText()) : Collections.emptyList();
        } else {
            return Collections.emptyList();
        }
    }

    @NotNull
    @Override
    public List<String> getParentNames() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.getParentNames();
        }
        if (SyncUtil.lockOrCancel(parentNamesLock)) {
            try {
                if (null == parentNames) {
                    PasClassParent classParent = getClassParent();
                    if (classParent != null) {
                        parentNames = new SmartList<>();
                        for (PasTypeID typeID : classParent.getTypeIDList()) {
                            parentNames.add(typeID.getFullyQualifiedIdent().getName());
                        }
                    } else {
                        parentNames = Collections.emptyList();
                    }
                }
            } finally {
                parentNamesLock.unlock();
            }
        }
        return parentNames;
    }

    // Returns structured type owning the field
    @Nullable
    @SuppressWarnings("unchecked")
    public static PascalStructType findOwnerStruct(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element,
                PasClassHelperDeclImpl.class, PasClassTypeDeclImpl.class, PasInterfaceTypeDeclImpl.class, PasObjectDeclImpl.class, PasRecordHelperDeclImpl.class, PasRecordDeclImpl.class);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    protected PsiElement getNameElement() {
        return PsiTreeUtil.getPrevSiblingOfType(getParent(), PasGenericTypeIdent.class);
    }

    /**
     * Returns structured type declaration element by its name element
     *
     * @param namedElement name element
     * @return structured type declaration element
     */
    @Nullable
    public static PasEntityScope getStructByNameElement(@NotNull final PascalNamedElement namedElement) {  // TODO: all scopes comes after name?
        PsiElement sibling = PsiUtil.getNextSibling(namedElement);
        sibling = sibling != null ? PsiUtil.getNextSibling(sibling) : null;
        if ((sibling instanceof PasTypeDecl) && (sibling.getFirstChild() instanceof PasEntityScope)) {
            return (PasEntityScope) sibling.getFirstChild();
        }
        return null;
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
                invalidateCaches();
                return EMPTY_MEMBERS;
            }
        }
    }

    @Nullable
    @Override
    public PasField getField(String name) {
        if (retrieveStub() != null) {
            return getFieldStub(name);
        } else {
            return getMembers(cache, MEMBER_BUILDER).all.get(name.toUpperCase());
        }
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        if (retrieveStub() != null) {
            return getAllFieldsStub();
        } else {
            return getMembers(cache, MEMBER_BUILDER).all.values();
        }
    }

    private PasField.Visibility getVisibility(PsiElement element) {
        StringBuilder sb = new StringBuilder();
        PsiElement psiChild = element.getFirstChild();
        while (psiChild != null) {
            if (psiChild.getClass() == LeafPsiElement.class) {
                sb.append(psiChild.getText().toUpperCase());
            }
            psiChild = psiChild.getNextSibling();
        }
        return STR_TO_VIS.get(sb.toString());
    }

    @Override
    public void invalidateCaches() {
        super.invalidateCaches();
        if (SyncUtil.lockOrCancel(parentNamesLock)) {
            parentNames = null;
            parentNamesLock.unlock();
        }
        if (SyncUtil.lockOrCancel(parentScopesLock)) {
            parentScopes = null;
            parentScopesLock.unlock();
        }
    }

    public static void invalidate(String key) {
        cache.invalidate(key);
    }

    private class MemberBuilder implements Callable<Members> {
        @Override
        public Members call() throws Exception {
            Members res = new Members();

            PasField.Visibility visibility = PasField.Visibility.PUBLISHED;
            PsiElement child = getFirstChild();
            while (child != null) {
                if (child.getClass() == PasClassFieldImpl.class) {
                    addFields(res, child, PasField.FieldType.VARIABLE, visibility);
                } else if (child.getClass() == PasConstSectionImpl.class) {                                                // nested constants
                    addFields(res, child, PasField.FieldType.CONSTANT, visibility);
                } else if (child.getClass() == PasTypeSectionImpl.class) {
                    addFields(res, child, PasField.FieldType.TYPE, visibility);
                } else if (child.getClass() == PasVarSectionImpl.class) {
                    addFields(res, child, PasField.FieldType.VARIABLE, visibility);
                } else if (child.getClass() == PasExportedRoutineImpl.class) {
                    addField(res, (PascalNamedElement) child, PasField.FieldType.ROUTINE, visibility);
                } else if (child.getClass() == PasClassPropertyImpl.class) {
                    addField(res, (PascalNamedElement) child, PasField.FieldType.PROPERTY, visibility);
                } else if (child.getClass() == PasVisibilityImpl.class) {
                    visibility = getVisibility(child);
                } else if (child.getClass() == PasRecordVariantImpl.class) {
                    addFields(res, child, PasField.FieldType.VARIABLE, visibility);
                } else if ((child instanceof PasNamedIdent) && (PasStubStructTypeImpl.this instanceof PasRecordDecl)) {
                    addField(res, (PascalNamedElement) child, PasField.FieldType.VARIABLE, visibility);
                }
                child = PsiTreeUtil.skipSiblingsForward(child, PsiWhiteSpace.class, PsiComment.class);
            }
            res.stamp = getStamp(getContainingFile());
            LOG.debug(getName() + ": buildMembers: " + res.all.size() + " members");
            return res;
        }
    }

    private void addFields(Members res, PsiElement element, PasField.FieldType fieldType, @NotNull PasField.Visibility visibility) {
        PsiElement child = element.getFirstChild();
        while (child != null) {
            if (child instanceof PasNamedIdent) {
                addField(res, (PascalNamedElement) child, fieldType, visibility);
            } else if (child.getClass() == PasConstDeclarationImpl.class) {
                addField(res, ((PasConstDeclarationImpl) child).getNamedIdentDecl(), fieldType, visibility);
            } else if (child instanceof PascalVariableDeclaration) {
                for (PascalNamedElement namedIdent : ((PascalVariableDeclaration) child).getNamedIdentDeclList()) {
                    addField(res, namedIdent, fieldType, visibility);
                }
            } else if (child.getClass() == PasTypeDeclarationImpl.class) {
                addField(res, ((PasTypeDeclarationImpl) child).getGenericTypeIdent(), fieldType, visibility);
                PasTypeDecl decl = ((PasTypeDeclarationImpl) child).getTypeDecl();
                PasEnumType enumType = decl != null ? decl.getEnumType() : null;
                if (enumType != null) {
                    for (PasNamedIdentDecl ident : enumType.getNamedIdentDeclList()) {
                        addField(res, ident, PasField.FieldType.CONSTANT, visibility);
                    }
                }
            } else if (child.getClass() == PasRecordVariantImpl.class) {
                addFields(res, child, fieldType, visibility);
            }
            child = PsiTreeUtil.skipSiblingsForward(child, PsiWhiteSpace.class, PsiComment.class);
        }
    }

    private void addField(Members res, @NotNull PascalNamedElement element, PasField.FieldType fieldType, @NotNull PasField.Visibility visibility) {
        addField(res, element, element.getName(), fieldType, visibility);
        if (fieldType == PasField.FieldType.ROUTINE) {
            addField(res, element, PsiUtil.getFieldName(element), fieldType, visibility);                        // add with signature included in name
        }
    }

    private PasField addField(Members res, PascalNamedElement element, String name, PasField.FieldType fieldType, @NotNull PasField.Visibility visibility) {
        PasField field = new PasField(this, element, name, fieldType, visibility);
        res.all.put(name.toUpperCase(), field);
        return field;
    }

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
        if (SyncUtil.lockOrCancel(parentScopesLock)) {
            try {
                if (null == parentScopes) {
                    calcParentScopes();
                }
            } finally {
                parentScopesLock.unlock();
            }
        }
        return parentScopes;
    }

    @NotNull
    @Override
    public Collection<PasWithStatement> getWithStatements() {
        return Collections.emptyList();
    }

    private void calcParentScopes() {
        calcParentScopesStub();
        if (null == parentScopes) {
            SmartList<SmartPsiElementPointer<PasEntityScope>> res = new SmartList<>();
            PasClassParent parent = getClassParent();
            if (parent != null) {
                for (PasTypeID typeID : parent.getTypeIDList()) {
                    NamespaceRec fqn = NamespaceRec.fromElement(typeID.getFullyQualifiedIdent());
                    PasEntityScope scope = PasReferenceUtil.resolveTypeScope(fqn, null, true);
                    if (scope != PasStubStructTypeImpl.this) {
                        addScope(res, scope);
                    }
                }
            }
            addDefaultScopes(res);
            parentScopes = res;
        }
    }

    private void calcParentScopesStub() {
        // TODO: cache with validation
        B stub = retrieveStub();
        if (stub != null) {
            List<SmartPsiElementPointer<PasEntityScope>> res;
            List<String> parentNames = stub.getParentNames();
            StubElement parentStub = stub.getParentStub();
            PsiElement parEl = parentStub != null ? parentStub.getPsi() : null;
            if ((parEl instanceof PascalClassDecl) || (parEl instanceof PascalInterfaceDecl)) {         // Nested type
                res = new SmartList<>(((PascalStructType) parEl).getParentScope());
                res.add(SmartPointerManager.getInstance(getProject()).createSmartPsiElementPointer((PasEntityScope) parEl));
            } else {
                res = new ArrayList<>(parentNames.size() + 1);
            }
            addDefaultScopes(res);
            Project project = getProject();
            final ResolveContext context = new ResolveContext(this.getContainingScope(), PasField.TYPES_TYPE, true,
                    null, ModuleUtil.retrieveUnitNamespaces(this));
            for (String parentName : parentNames) {
                Collection<PasField> types = ResolveUtil.resolveWithStubs(NamespaceRec.fromFQN(this, parentName + ResolveUtil.STRUCT_SUFFIX),
                        context, 0);
                for (PasField type : types) {
                    PascalNamedElement el = type.getElement();
                    if (el instanceof PasEntityScope) {
                        res.add(SmartPointerManager.getInstance(project).createSmartPsiElementPointer((PasEntityScope) el));
                    }
                }
            }
            parentScopes = Collections.unmodifiableList(res);
        }
    }

    private void addDefaultScopes(List<SmartPsiElementPointer<PasEntityScope>> scopes) {
        PasEntityScope defEntity = null;
        PasEntityScope scope = this.getContainingScope();
        scope = scope != null ? scope : this;
        if (this instanceof PasClassTypeDecl) {
            defEntity = PasReferenceUtil.resolveTypeScope(NamespaceRec.fromFQN(scope, "system.TObject"), scope, true);
        } else if (this instanceof PasInterfaceTypeDecl) {
            defEntity = PasReferenceUtil.resolveTypeScope(NamespaceRec.fromFQN(scope, "system.IInterface"), scope, true);
        }
        if (defEntity != this) {
            addScope(scopes, defEntity);
        }
    }

    private static void addScope(List<SmartPsiElementPointer<PasEntityScope>> scopes, PasEntityScope scope) {
        if (scope != null) {
            scopes.add(SmartPointerManager.getInstance(scope.getProject()).createSmartPsiElementPointer(scope));
        }
    }
}
