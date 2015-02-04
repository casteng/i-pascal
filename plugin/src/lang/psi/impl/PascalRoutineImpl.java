package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.FieldCollector;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 06/09/2013
 */
public abstract class PascalRoutineImpl extends PascalNamedElementImpl implements PasEntityScope {
    private Map<String, PasField> members;
    private Set<PascalNamedElement> redeclaredMembers = null;
    private long buildStamp = 0;
    //private List<PasFormalParameter> formalParameters;

    @Nullable
    public abstract PasFormalParameterSection getFormalParameterSection();

    public PascalRoutineImpl(ASTNode node) {
        super(node);
    }

    public boolean isInterface() {
        return (getClass() == PasExportedRoutineImpl.class) || (getClass() == PasClassMethodImpl.class);
    }

    @Nullable
    @Override
    public PasField getField(String name) {
        if (!isCacheActual(members, buildStamp)) {
            buildMembers();
        }
        return members.get(name);
    }

    synchronized private void buildMembers() {
        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return;
        }
        if (isCacheActual(members, buildStamp)) { return; }  // TODO: check correctness
        buildStamp = getContainingFile().getModificationStamp();
        members = new LinkedHashMap<String, PasField>();

        redeclaredMembers = new LinkedHashSet<PascalNamedElement>();
        System.out.println("routine buildMembers: " + getName());

        List<PasFormalParameter> params = PsiUtil.getFormalParameters(getFormalParameterSection());
        for (PasFormalParameter parameter : params) {
            addField(parameter, PasField.Type.VARIABLE);
        }

        //noinspection unchecked
        PsiUtil.retrieveEntitiesFromSection(this, this, PasField.Visibility.STRICT_PRIVATE,
                new FieldCollector() {
                    @Override
                    public boolean fieldExists(PascalNamedElement element) {
                        if (members.containsKey(element.getName())) {
                            redeclaredMembers.add(element);
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void addField(String name, PasField field) {
                        members.put(name, field);
                    }
                },
                PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class);
        System.out.println(getName() + ": buildMembers: " + members.size() + "members");
    }

    private void addField(PascalNamedElement element, PasField.Type type) {
        PasField field = new PasField(this, element, element.getName(), type, PasField.Visibility.STRICT_PRIVATE);
        members.put(field.name, field);
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        if (!isCacheActual(members, buildStamp)) {
            buildMembers();
        }
        return members.values();
    }

    public boolean isCacheActual(Map<String, PasField> cache, long stamp) {
        return (cache != null) && (getContainingFile() != null) && (getContainingFile().getModificationStamp() == stamp);
    }

    public PasFullyQualifiedIdent getFunctionTypeIdent() {
        PasTypeDecl type = PsiTreeUtil.getChildOfType(this, PasTypeDecl.class);
        return PsiTreeUtil.findChildOfType(type, PasFullyQualifiedIdent.class);
    }
}
