package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
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
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.struct.PasStructStub;
import com.siberika.idea.pascal.util.PsiUtil;
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
        extends PascalStubStructTypeImpl<B> implements PascalStructType<B> {

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

    public PasStubStructTypeImpl(ASTNode node) {
        super(node);
    }

    public PasStubStructTypeImpl(final B stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @NotNull
    @Override
    synchronized public List<String> getParentNames() {
        B stub = getStub();
        if (stub != null) {
            return stub.getParentNames();
        }
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
        PasTypeDeclaration typeDecl = PsiTreeUtil.getParentOfType(this, PasTypeDeclaration.class);
        return PsiUtil.findImmChildOfAnyType(typeDecl, PasGenericTypeIdentImpl.class);
    }

    /**
     * Returns structured type declaration element by its name element
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
                invalidateCaches(getKey());
                return EMPTY_MEMBERS;
            }
        }
    }

    @Nullable
    @Override
    public PasField getField(String name) {
        if (getStub() != null) {
            return getFieldStub(name);
        } else {
            return getMembers(cache, MEMBER_BUILDER).all.get(name.toUpperCase());
        }
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        if (getStub() != null) {
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
    public void subtreeChanged() {
        super.subtreeChanged();
        invalidateCached();
    }

    synchronized private void invalidateCached() {
        parentNames = null;
        parentScopes = null;
    }

    public static void invalidate(String key) {
        cache.invalidate(key);
        parentCache.invalidate(key);
    }

    private class MemberBuilder implements Callable<Members> {
        @Override
        public Members call() throws Exception {
            if (null == getContainingFile()) {
                PascalPsiImplUtil.logNullContainingFile(PasStubStructTypeImpl.this);
                return null;
            }
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
            } else if (child.getClass() == PasVarDeclarationImpl.class) {
                for (PasNamedIdent namedIdent : ((PasVarDeclarationImpl) child).getNamedIdentDeclList()) {
                    addField(res, namedIdent, fieldType, visibility);
                }
            } else if (child.getClass() == PasTypeDeclarationImpl.class) {
                addField(res, ((PasTypeDeclarationImpl) child).getGenericTypeIdent(), fieldType, visibility);
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
        List<SmartPsiElementPointer<PasEntityScope>> res = retrieveParentScopesStub();
        if (res != null) {
            return res;
        }
        ensureChache(parentCache);
        try {
            return parentCache.get(getKey(), new ParentBuilder()).scopes;
        } catch (Exception e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.warn("Error occured during building members for: " + this, e.getCause());
                invalidateCaches(getKey());
                return Collections.emptyList();
            }
        }
    }

    synchronized private List<SmartPsiElementPointer<PasEntityScope>> retrieveParentScopesStub() {
        if (null == parentScopes) {
            calcParentScopesStub();
        }
        return parentScopes;
    }

    private void calcParentScopesStub() {
        // TODO: cache with validation
        B stub = getStub();
        if (stub != null) {
            List<String> parentNames = stub.getParentNames();
            StubElement parentStub = stub.getParentStub();
            PsiElement parEl = parentStub != null ? parentStub.getPsi() : null;
            if (parEl instanceof PascalStructType) {
                parentScopes = ((PascalStructType) parEl).getParentScope();
                parentScopes.add(SmartPointerManager.createPointer((PasEntityScope) parEl));
            } else {
                parentScopes = new ArrayList<>(parentNames.size() + 1);
            }
            for (String parentName : parentNames) {                            // TODO: +TObject
                Collection<PasField> types = ResolveUtil.resolveWithStubs(NamespaceRec.fromFQN(this, parentName + ResolveUtil.STRUCT_SUFFIX),
                        new ResolveContext(this, PasField.TYPES_TYPE, true, null), 0);
                for (PasField type : types) {
                    PascalNamedElement el = type.getElement();
                    if (el instanceof PasEntityScope) {
                        parentScopes.add(SmartPointerManager.createPointer((PasEntityScope) el));
                    }
                }
            }
        }
    }

    private class ParentBuilder implements Callable<Parents> {
        @Override
        public Parents call() throws Exception {
            if (null == getContainingFile()) {
                PascalPsiImplUtil.logNullContainingFile(PasStubStructTypeImpl.this);
                return null;
            }
            Parents res = new Parents();
            res.stamp = getStamp(getContainingFile());
            res.scopes = new SmartList<SmartPsiElementPointer<PasEntityScope>>();
            PasClassParent parent = getClassParent();
            if (parent != null) {
                for (PasTypeID typeID : parent.getTypeIDList()) {
                    NamespaceRec fqn = NamespaceRec.fromElement(typeID.getFullyQualifiedIdent());
                    PasEntityScope scope = PasReferenceUtil.resolveTypeScope(fqn, true);
                    if (scope != PasStubStructTypeImpl.this) {
                        addScope(res, scope);
                    }
                }
            } else {
                PasEntityScope defEntity = null;
                if (PasStubStructTypeImpl.this instanceof PasClassTypeDecl) {
                    defEntity = PasReferenceUtil.resolveTypeScope(NamespaceRec.fromFQN(PasStubStructTypeImpl.this, "system.TObject"), true);
                } else if (PasStubStructTypeImpl.this instanceof PasInterfaceTypeDecl) {
                    defEntity = PasReferenceUtil.resolveTypeScope(NamespaceRec.fromFQN(PasStubStructTypeImpl.this, "system.IInterface"), true);
                }
                if (defEntity != PasStubStructTypeImpl.this) {
                    addScope(res, defEntity);
                }
            }

            return res;
        }

        private void addScope(Parents res, PasEntityScope scope) {
            if (scope != null) {
                res.scopes.add(SmartPointerManager.getInstance(scope.getProject()).createSmartPsiElementPointer(scope));
            }
        }

    }

}
