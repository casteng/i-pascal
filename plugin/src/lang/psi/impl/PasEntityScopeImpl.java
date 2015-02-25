package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
public abstract class PasEntityScopeImpl extends PascalNamedElementImpl implements PasEntityScope {

    public static final Logger LOG = Logger.getInstance(PasEntityScopeImpl.class.getName());

    private List<Map<String, PasField>> members = null;
    private Set<PascalNamedElement> redeclaredMembers = null;
    private long buildStamp = 0;

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

    private boolean cacheStale;

    public PasEntityScopeImpl(ASTNode node) {
        super(node);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static PasEntityScopeImpl findOwner(PsiElement element) {
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
    public static PasEntityScope getStructByNameElement(final PascalNamedElement namedElement) {  // TODO: all scopes comes after name?
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
        if (!isCacheActual(members, buildStamp)) { // TODO: check correctness
            buildMembers();
        }
        for (PasField.Visibility visibility : PasField.Visibility.values()) {
            PasField result = members.get(visibility.ordinal()).get(name);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
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

    synchronized private void buildMembers() {
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
        redeclaredMembers = new LinkedHashSet<PascalNamedElement>();

        PasField.Visibility visibility = PasField.Visibility.PUBLISHED;
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child.getClass() == PasClassFieldImpl.class) {
                addFields(child, visibility);
            } else if (child.getClass() == PasExportedRoutineImpl.class) {
                addField((PascalNamedElement) child, PasField.Type.ROUTINE, visibility);
            } else if (child.getClass() == PasClassPropertyImpl.class) {
                addField((PascalNamedElement) child, PasField.Type.PROPERTY, visibility);
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
                addField((PascalNamedElement) child, PasField.Type.VARIABLE, visibility);
            }
            child = child.getNextSibling();
        }
    }

    private void addField(PascalNamedElement element, PasField.Type type, @NotNull PasField.Visibility visibility) {
        PasField field = new PasField(this, element, element.getName(), type, visibility);
        if (members.get(visibility.ordinal()) == null) {
            members.set(visibility.ordinal(), new LinkedHashMap<String, PasField>());
        }
        members.get(visibility.ordinal()).put(field.name, field);
    }

    public boolean isCacheActual(List<Map<String, PasField>> cache, long stamp) {
        return (getContainingFile() != null) && (cache != null) && (getContainingFile().getModificationStamp() == stamp);
    }

    @Nullable
    @Override
    public PasFullyQualifiedIdent getParentScope() {
        PasClassParent parent = null;
        if (getClass() == PasClassTypeDeclImpl.class) {
            parent = ((PasClassTypeDeclImpl) this).getClassParent();
        }

        if (parent != null) {
            for (PasTypeID typeID : parent.getTypeIDList()) {
                return typeID.getFullyQualifiedIdent();
            }
        }

        return null;
    }
}
