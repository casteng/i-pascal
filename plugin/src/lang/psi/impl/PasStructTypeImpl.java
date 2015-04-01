package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
public abstract class PasStructTypeImpl extends PasScopeImpl implements PasEntityScope {

    public static final Logger LOG = Logger.getInstance(PasStructTypeImpl.class.getName());

    private List<Map<String, PasField>> members = null;

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

    @Nullable
    @Override
    synchronized public PasField getField(String name) throws PasInvalidScopeException {
        if (!isCacheActual(members, buildStamp)) { // TODO: check correctness
            buildMembers();
        }
        for (PasField.Visibility visibility : PasField.Visibility.values()) {
            PasField result = members.get(visibility.ordinal()).get(name.toUpperCase());
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    @NotNull
    @Override
    synchronized public Collection<PasField> getAllFields() throws PasInvalidScopeException {
        if (!PsiUtil.isElementValid(this)) {
            PsiUtil.rebuildPsi(this.getContainingFile());
            return Collections.emptyList();
            //throw new PasInvalidScopeException(this);
        }
        if (!isCacheActual(members, buildStamp)) {
            buildMembers();
        }
        Collection<PasField> result = new HashSet<PasField>();
        for (Map<String, PasField> fields : members) {
            result.addAll(fields.values());
        }
        return result;
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

    private void buildMembers() throws PasInvalidScopeException {
        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return;
        }
        if (isCacheActual(members, buildStamp)) {
            return;
        }  // TODO: check correctness
        members = new ArrayList<Map<String, PasField>>(PasField.Visibility.values().length);
        for (PasField.Visibility visibility : PasField.Visibility.values()) {
            members.add(visibility.ordinal(), new LinkedHashMap<String, PasField>());
        }
        assert members.size() == PasField.Visibility.values().length;

        addField(this, "Self", PasField.FieldType.VARIABLE, PasField.Visibility.PRIVATE);
        PasField.Visibility visibility = PasField.Visibility.PUBLISHED;
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child.getClass() == PasClassFieldImpl.class) {
                addFields(child, visibility);
            } else if (child.getClass() == PasExportedRoutineImpl.class) {
                addField((PascalNamedElement) child, PasField.FieldType.ROUTINE, visibility);
            } else if (child.getClass() == PasClassPropertyImpl.class) {
                addField((PascalNamedElement) child, PasField.FieldType.PROPERTY, visibility);
            } else if (child.getClass() == PasVisibilityImpl.class) {
                visibility = getVisibility(child);
            } else if (child.getClass() == PasRecordVariantImpl.class) {
                addFields(child, visibility);
            }
            child = child.getNextSibling();
        }
        buildStamp = getContainingFile().getModificationStamp();
        System.out.println(getName() + ": buildMembers: " + members.size() + " members");
    }

    private void addFields(PsiElement element, @NotNull PasField.Visibility visibility) {
        PsiElement child = element.getFirstChild();
        while (child != null) {
            if (child.getClass() == PasNamedIdentImpl.class) {
                addField((PascalNamedElement) child, PasField.FieldType.VARIABLE, visibility);
            }
            child = child.getNextSibling();
        }
    }

    private void addField(PascalNamedElement element, PasField.FieldType fieldType, @NotNull PasField.Visibility visibility) {
        addField(element, element.getName(), fieldType, visibility);
    }

    private void addField(PascalNamedElement element, String name, PasField.FieldType fieldType, @NotNull PasField.Visibility visibility) {
        PasField field = new PasField(this, element, name, fieldType, visibility);
        if (members.get(visibility.ordinal()) == null) {
            members.set(visibility.ordinal(), new LinkedHashMap<String, PasField>());
        }
        members.get(visibility.ordinal()).put(name.toUpperCase(), field);
    }

    @NotNull
    @Override
    synchronized public List<PasEntityScope> getParentScope() {
        if (null == parentScopes) {
            buildParentScopes();
        }
        return parentScopes;
    }

    private void buildParentScopes() {
        NamespaceRec fqn = null;
        PasClassParent parent = null;
        if (getClass() == PasClassTypeDeclImpl.class) {
            parent = ((PasClassTypeDeclImpl) this).getClassParent();
        }
        parentScopes = new SmartList<PasEntityScope>();
        if (parent != null) {
            for (PasTypeID typeID : parent.getTypeIDList()) {
                fqn = NamespaceRec.fromElement(typeID.getFullyQualifiedIdent());
            }
        } else if (!PsiUtil.isFromSystemUnit(this)) {
            fqn = NamespaceRec.fromFQN(this, "system.TObject");
        }
        if (fqn != null) {
            PasEntityScope scope = PasReferenceUtil.resolveTypeScope(fqn, true);
            if (scope != null) {
                parentScopes.add(scope);
            }
        }
    }

    @Override
    synchronized public void invalidateCache() {
        System.out.println("*** invalidating cache");
        members = null;
    }
}
