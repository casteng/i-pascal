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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
public abstract class PasStructTypeImpl extends PasScopeImpl implements PasEntityScope {

    public static final Logger LOG = Logger.getInstance(PasStructTypeImpl.class.getName());

    private static final Cache<String, Members> cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    private static final Map<String, PasField.Visibility> STR_TO_VIS;

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

    public PasStructTypeImpl(ASTNode node) {
        super(node);
    }

    // Returns structured type owning the field
    @Nullable
    @SuppressWarnings("unchecked")
    public static PasStructTypeImpl findOwnerStruct(PsiElement element) {
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
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.error("Error occured during building members for: " + this, e.getCause());
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

    public static void invalidate(String key) {
        cache.invalidate(key);
        parentCache.invalidate(key);
    }

    private class MemberBuilder implements Callable<Members> {
        @Override
        public Members call() throws Exception {
            if (null == getContainingFile()) {
                PascalPsiImplUtil.logNullContainingFile(PasStructTypeImpl.this);
                return null;
            }
            if (building) {
                LOG.info("WARNING: Reentered in buildXXX");
                //return null;
            }
            building = true;
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
                }
                child = PsiTreeUtil.skipSiblingsForward(child, PsiWhiteSpace.class, PsiComment.class);
            }
            res.stamp = getStamp(getContainingFile());
            LOG.info(getName() + ": buildMembers: " + res.all.size() + " members");
            building = false;
            return res;
        }
    }

    private void addFields(Members res, PsiElement element, PasField.FieldType fieldType, @NotNull PasField.Visibility visibility) {
        PsiElement child = element.getFirstChild();
        while (child != null) {
            if (child.getClass() == PasNamedIdentImpl.class) {
                addField(res, (PascalNamedElement) child, fieldType, visibility);
            } else if (child.getClass() == PasConstDeclarationImpl.class) {
                addField(res, ((PasConstDeclarationImpl) child).getNamedIdent(), fieldType, visibility);
            } else if (child.getClass() == PasVarDeclarationImpl.class) {
                for (PasNamedIdent namedIdent : ((PasVarDeclarationImpl) child).getNamedIdentList()) {
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
        ensureChache(parentCache);
        try {
            return parentCache.get(getKey(), new ParentBuilder()).scopes;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.error("Error occured during building members for: " + this, e.getCause());
                return Collections.emptyList();
            }
        }
    }

    public abstract PasClassParent getClassParent();

    private class ParentBuilder implements Callable<Parents> {
        @Override
        public Parents call() throws Exception {
            if (null == getContainingFile()) {
                PascalPsiImplUtil.logNullContainingFile(PasStructTypeImpl.this);
                return null;
            }
            Parents res = new Parents();
            res.stamp = getStamp(getContainingFile());

            res.stamp = getStamp(getContainingFile());
            res.scopes = new SmartList<SmartPsiElementPointer<PasEntityScope>>();
            PasClassParent parent = getClassParent();
            if (parent != null) {
                for (PasTypeID typeID : parent.getTypeIDList()) {
                    NamespaceRec fqn = NamespaceRec.fromElement(typeID.getFullyQualifiedIdent());
                    PasEntityScope scope = PasReferenceUtil.resolveTypeScope(fqn, true);
                    addScope(res, scope);
                }
            } else {
                PasEntityScope defEntity = null;
                if (this instanceof PasClassTypeDecl) {
                    defEntity = PasReferenceUtil.resolveTypeScope(NamespaceRec.fromFQN(PasStructTypeImpl.this, "system.TObject"), true);
                } else if (this instanceof PasInterfaceTypeDecl) {
                    defEntity = PasReferenceUtil.resolveTypeScope(NamespaceRec.fromFQN(PasStructTypeImpl.this, "system.IInterface"), true);
                }
                if ((defEntity != null) && (defEntity != this)) {
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
