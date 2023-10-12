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
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasConstrainedTypeParam;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
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
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.PascalVariableDeclaration;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PasExportedRoutineStub;
import com.siberika.idea.pascal.lang.stub.struct.PasStructStub;
import com.siberika.idea.pascal.module.ModuleService;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class PasStubStructTypeImpl<T extends PascalStructType, B extends PasStructStub<T>>
        extends PasStubScopeImpl<B> implements PascalStructType<B> {

    private static final Logger LOG = Logger.getInstance(PasStubStructTypeImpl.class);

    private static final Cache<String, PascalHelperScope.Members> cache = CacheBuilder.newBuilder().softValues().build();

    private static final Map<String, PasField.Visibility> STR_TO_VIS;

    private final Callable<? extends PascalHelperScope.Members> MEMBER_BUILDER = this.new MemberBuilder();

    static {
        STR_TO_VIS = new HashMap<>(PasField.Visibility.values().length);
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

    volatile private List<String> parentNames = null;
    volatile private List<SmartPsiElementPointer<PasEntityScope>> parentScopes;

    public PasStubStructTypeImpl(ASTNode node) {
        super(node);
    }

    public PasStubStructTypeImpl(final B stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    public void invalidateCache(boolean subtreeChanged) {
        super.invalidateCache(subtreeChanged);
        parentNames = null;
        parentScopes = null;
    }

    static void invalidate(String key) {
        cache.invalidate(key);
    }

    @NotNull
    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.TYPE;
    }

    @NotNull
    @Override
    public List<String> getTypeParameters() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.getTypeParameters();
        }
        PsiElement nameElement = getNameIdentifier();
        if (nameElement instanceof PasGenericTypeIdent) {
            List<PasConstrainedTypeParam> typeParams = ((PasGenericTypeIdent) nameElement).getConstrainedTypeParamList();
            List<String> res = new SmartList<>();
            for (PasConstrainedTypeParam typeParam : typeParams) {
                for (PasNamedIdent ident : typeParam.getNamedIdentList()) {
                    res.add(ident.getName());
                }
            }
            return res;
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
        if (null == parentNames) {
            parentNames = calcParentNames();
        }
        return parentNames;
    }

    @NotNull
    @Override
    public String getCanonicalTypeName() {
        String name = getName();
        if (name.indexOf('<') >= 0) {
            name = name.substring(0, name.indexOf('<')) + '<' + String.join(", ", getTypeParameters()) + '>';
        }
        return name;
    }

    // Returns structured type owning the field
    @Nullable
    @SuppressWarnings("unchecked")
    public static PascalStructType findOwnerStruct(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element,
                PasClassHelperDeclImpl.class, PasClassTypeDeclImpl.class, PasInterfaceTypeDeclImpl.class, PasObjectDeclImpl.class, PasRecordHelperDeclImpl.class, PasRecordDeclImpl.class);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
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

    @Nullable
    @Override
    public PasField getField(String name) {
        if (retrieveStub() != null) {
            return getFieldStub(name);
        } else {
            return getMembers(cache, MEMBER_BUILDER).all.get(name.toUpperCase());
        }
    }

    @Nullable
    @Override
    public PascalRoutine getRoutine(String reducedName) {
        return RoutineUtil.findRoutine(getAllFields(), reducedName);
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

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
        if (null == parentScopes) {
            parentScopes = calcParentScopes();
        }
        return parentScopes;
    }

    @NotNull
    @Override
    public Collection<PasWithStatement> getWithStatements() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<PasExportedRoutine> getMethods() {
        B stub = retrieveStub();
        if (stub != null) {
            List<PasExportedRoutine> res = new SmartList<>();
            for (StubElement childrenStub : stub.getChildrenStubs()) {
                if (childrenStub instanceof PasExportedRoutineStub) {
                    res.add((PasExportedRoutine) childrenStub.getPsi());
                }
            }
            return res;
        } else {
            return getExportedRoutineList();
        }
    }

    @NotNull
    private PascalHelperScope.Members getMembers(Cache<String, PascalHelperScope.Members> cache, Callable<? extends PascalHelperScope.Members> builder) {
        ensureChache(cache);
        try {
            return cache.get(getKey(), builder);
        } catch (Exception e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.warn("Error occured during building members for: " + this, e.getCause());
                invalidateCache(false);
                return PascalHelperScope.EMPTY_MEMBERS;
            }
        }
    }

    private List<String> calcParentNames() {
        PasClassParent classParent = getClassParent();
        if (classParent != null) {
            List<String> result = new SmartList<>();
            for (PasTypeID typeID : classParent.getTypeIDList()) {
                result.add(typeID.getFullyQualifiedIdent().getName());
            }
            return result;
        } else {
            return Collections.emptyList();
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

    private class MemberBuilder implements Callable<PascalHelperScope.Members> {
        @Override
        public PascalHelperScope.Members call() throws Exception {
            PascalHelperScope.Members res = new PascalHelperScope.Members();

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
                } else if (child instanceof PasClassProperty) {
                    PasNamedIdentDecl namedIdentDecl = ((PasClassProperty) child).getNamedIdentDecl();
                    if (namedIdentDecl != null) {
                        addField(res, namedIdentDecl, PasField.FieldType.PROPERTY, visibility);
                    }
                } else if (child.getClass() == PasVisibilityImpl.class) {
                    visibility = getVisibility(child);
                } else if (child.getClass() == PasRecordVariantImpl.class) {
                    addFields(res, child, PasField.FieldType.VARIABLE, visibility);
                } else if ((child instanceof PasNamedIdent) && (PasStubStructTypeImpl.this instanceof PasRecordDecl)) {
                    addField(res, (PascalNamedElement) child, PasField.FieldType.VARIABLE, visibility);
                }
                child = PsiTreeUtil.skipSiblingsForward(child, PsiWhiteSpace.class, PsiComment.class);
            }
            // Add type parameters to this structured type scope
            PsiElement nameIdent = getNameIdentifier();
            if (nameIdent instanceof PasGenericTypeIdent) {
                List<PasConstrainedTypeParam> typeParams = ((PasGenericTypeIdent) nameIdent).getConstrainedTypeParamList();
                for (PasConstrainedTypeParam typeParam : typeParams) {
                    for (PasNamedIdent typeParamIdent : typeParam.getNamedIdentList()) {
                        addField(res, typeParamIdent, PasField.FieldType.TYPE, PasField.Visibility.STRICT_PRIVATE);
                    }
                }
            }
            res.stamp = getStamp(getContainingFile());
            LOG.debug(getName() + ": buildMembers: " + res.all.size() + " members");
            return res;
        }
    }

    private void addFields(PascalHelperScope.Members res, PsiElement element, PasField.FieldType fieldType, @NotNull PasField.Visibility visibility) {
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

    private void addField(PascalHelperScope.Members res, @NotNull PascalNamedElement element, PasField.FieldType fieldType, @NotNull PasField.Visibility visibility) {
        addField(res, element, element.getName(), fieldType, visibility);
        if (fieldType == PasField.FieldType.ROUTINE) {
            addField(res, element, PsiUtil.getFieldName(element), fieldType, visibility);                        // add with signature included in name
        }
    }

    private PasField addField(PascalHelperScope.Members res, PascalNamedElement element, String name, PasField.FieldType fieldType, @NotNull PasField.Visibility visibility) {
        PasField field = new PasField(this, element, name, fieldType, visibility);
        res.all.put(name.toUpperCase(), field);
        return field;
    }

    private List<SmartPsiElementPointer<PasEntityScope>> calcParentScopes() {
        List<SmartPsiElementPointer<PasEntityScope>> res = calcParentScopesStub();
        if (res != null) {
            // ===*** debug validation
            for (SmartPsiElementPointer<PasEntityScope> scopePtr : res) {
                if (null == scopePtr) {
                    LOG.info("ERROR: parent is null for " + StrUtil.toDebugString(this));
                }
            }
            return res;
        }
        res = new SmartList<>();
        boolean noClassParents = true;
        PasEntityScope containing = getContainingScope();
        if ((containing instanceof PascalClassDecl) || (containing instanceof PascalInterfaceDecl)) {         // Nested type
            addScope(res, containing);
        }
        PasClassParent parent = getClassParent();
        if (parent != null) {
            for (PasTypeID typeID : parent.getTypeIDList()) {
                NamespaceRec fqn = NamespaceRec.fromElement(typeID.getFullyQualifiedIdent());
                PasEntityScope scope = PasReferenceUtil.resolveTypeScope(fqn, null, true);
                if (scope != PasStubStructTypeImpl.this) {
                    noClassParents &= !(scope instanceof PasClassTypeDecl);
                    addScope(res, scope);
                }
            }
        }
        getDefaultParentScope(res, noClassParents);
        return res;
    }

    private List<SmartPsiElementPointer<PasEntityScope>> calcParentScopesStub() {
        // TODO: cache with validation
        B stub = retrieveStub();
        if (stub != null) {
            List<SmartPsiElementPointer<PasEntityScope>> res;
            boolean noClassParents = true;
            List<String> parentNames = stub.getParentNames();
            StubElement parentStub = stub.getParentStub();
            PsiElement parEl = parentStub != null ? parentStub.getPsi() : null;
            if ((parEl instanceof PascalClassDecl) || (parEl instanceof PascalInterfaceDecl)) {         // Nested type
                res = new SmartList<>();
                res.add(SmartPointerManager.getInstance(getProject()).createSmartPsiElementPointer((PasEntityScope) parEl));
            } else {
                res = new ArrayList<>(parentNames.size() + 1);
            }
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
                        noClassParents &= !(el instanceof PasClassTypeDecl);
                    }
                }
            }
            getDefaultParentScope(res, noClassParents);
            return Collections.unmodifiableList(res);
        }
        return null;
    }

    private void getDefaultParentScope(List<SmartPsiElementPointer<PasEntityScope>> res, boolean noClassParents) {
        if (res.isEmpty() && this instanceof PasInterfaceTypeDecl) {
            PasEntityScope iInterface = resolveWithCache("system.IInterface");
            if (iInterface != this) {
                addScope(res, iInterface);
            }
        }
        if (noClassParents && this instanceof PasClassTypeDecl) {
            PasEntityScope tObject = resolveWithCache("system.TObject");
            if (tObject != this) {
                addScope(res, tObject);
            }
        }
    }

    private PasEntityScope resolveWithCache(String key) {
        return ModuleService.getInstance(com.intellij.openapi.module.ModuleUtil.findModuleForPsiElement(this))
                .calcWithCache(key, () -> {
                    PasEntityScope scope = PasStubStructTypeImpl.this.getContainingScope();
                    scope = scope != null ? scope : PasStubStructTypeImpl.this;
                    return PasReferenceUtil.resolveTypeScope(NamespaceRec.fromFQN(scope, key), scope, true);
                });
    }

    // returns True if scope is a class
    private static boolean addScope(List<SmartPsiElementPointer<PasEntityScope>> scopes, PasEntityScope scope) {
        if (scope != null) {
            scopes.add(SmartPointerManager.getInstance(scope.getProject()).createSmartPsiElementPointer(scope));
        }
        return scope instanceof PasClassTypeDecl;
    }

    @Override
    protected void initAllFlags() {
        super.initAllFlags();
    }

    @Override
    @Nullable
    public PsiElement getPasName() {
        return getNameIdentifier();
    }
}
