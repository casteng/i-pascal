package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.FieldCollector;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 06/09/2013
 */
public abstract class PascalRoutineImpl extends PasScopeImpl implements PasEntityScope {
    public static final String BUILTIN_RESULT = "Result";
    private Map<String, PasField> members;
    private Set<PascalNamedElement> redeclaredMembers = null;

    @Nullable
    public abstract PasFormalParameterSection getFormalParameterSection();

    public PascalRoutineImpl(ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    synchronized public PasField getField(String name) throws PasInvalidScopeException {
        if (!isCacheActual(members, buildStamp)) {
            buildMembers();
        }
        return members.get(name.toUpperCase());
    }

    private void buildMembers() throws PasInvalidScopeException {
        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return;
        }
        if (isCacheActual(members, buildStamp)) { return; }  // TODO: check correctness
        buildStamp = getContainingFile().getModificationStamp();
        members = new LinkedHashMap<String, PasField>();

        redeclaredMembers = new LinkedHashSet<PascalNamedElement>();

        List<PasNamedIdent> params = PsiUtil.getFormalParameters(getFormalParameterSection());
        for (PasNamedIdent parameter : params) {
            addField(parameter, PasField.FieldType.VARIABLE);
        }

        //noinspection unchecked
        PsiUtil.processEntitiesInSection(this, this, PasField.Visibility.STRICT_PRIVATE,
                new FieldCollector() {
                    @Override
                    public boolean fieldExists(PascalNamedElement element) {
                        if (members.containsKey(element.getName().toUpperCase())) {
                            redeclaredMembers.add(element);
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void addField(String name, PasField field) {
                        members.put(name.toUpperCase(), field);
                    }
                },
                PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class
        );
        if (!members.containsKey(BUILTIN_RESULT.toUpperCase())) {
            members.put(BUILTIN_RESULT.toUpperCase(), new PasField(this, this, BUILTIN_RESULT, PasField.FieldType.VARIABLE, PasField.Visibility.PRIVATE));
        }
        //System.out.println(getName() + ": buildMembers: " + members.size() + " members");
    }

    private void addField(PascalNamedElement element, PasField.FieldType fieldType) {
        PasField field = new PasField(this, element, element.getName(), fieldType, PasField.Visibility.STRICT_PRIVATE);
        members.put(field.name.toUpperCase(), field);
    }

    @NotNull
    @Override
    synchronized public Collection<PasField> getAllFields() throws PasInvalidScopeException {
        if (!isCacheActual(members, buildStamp)) {
            buildMembers();
        }
        return members.values();
    }

    public PasFullyQualifiedIdent getFunctionTypeIdent() {
        PasTypeDecl type = PsiTreeUtil.getChildOfType(this, PasTypeDecl.class);
        return PsiTreeUtil.findChildOfType(type, PasFullyQualifiedIdent.class);
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
        PasClassQualifiedIdent ident = PsiTreeUtil.getChildOfType(this, PasClassQualifiedIdent.class);
        if ((ident != null) && (ident.getSubIdentList().size() > 1)) {          // Should contain at least class name and method name parts
            NamespaceRec fqn = NamespaceRec.fromElement(ident.getSubIdentList().get(ident.getSubIdentList().size() - 2));
            parentScopes = Collections.emptyList();                             // To prevent infinite recursion
            PasEntityScope type = PasReferenceUtil.resolveTypeScope(fqn, true);
            if (type != null) {
                parentScopes = Collections.singletonList(type);
            }
        } else {
            parentScopes = Collections.emptyList();
        }
    }

    @Override
    synchronized public void invalidateCache() {
        System.out.println("*** invalidating cache");
        members = null;
    }
}
