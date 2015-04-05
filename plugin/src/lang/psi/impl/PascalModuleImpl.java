package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.FieldCollector;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 14/09/2013
 */
public class PascalModuleImpl extends PascalNamedElementImpl implements PasEntityScope {

    public static final Logger LOG = Logger.getInstance(PascalModuleImpl.class.getName());

    private Map<String, PasField> privateMembers = null;
    private Map<String, PasField> publicMembers = null;
    private Set<PascalNamedElement> redeclaredPrivateMembers = null;
    private Set<PascalNamedElement> redeclaredPublicMembers = null;
    private List<PasEntityScope> privateUnits = Collections.emptyList();
    private List<PasEntityScope> publicUnits = Collections.emptyList();
    private long buildPrivateStamp = -1;
    private long buildPublicStamp = -1;

    public PascalModuleImpl(ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    synchronized public final PasField getField(final String name) throws PasInvalidScopeException {
        if (!isCacheActual(publicMembers, buildPublicStamp)) {
            buildPublicMembers();
        }
        PasField result = publicMembers.get(name.toUpperCase());
        if (null == result) {
            if (!isCacheActual(privateMembers, buildPrivateStamp)) {
                buildPrivateMembers();
            }
            result = privateMembers.get(name.toUpperCase());
        }
        return result;
    }

    @NotNull
    @Override
    synchronized public Collection<PasField> getAllFields() throws PasInvalidScopeException {
        if (!PsiUtil.checkeElement(this)) {
            return Collections.emptyList();
        }
        if (!isCacheActual(publicMembers, buildPublicStamp)) {
            buildPublicMembers();
        }
        if (!isCacheActual(privateMembers, buildPrivateStamp)) {
            buildPrivateMembers();
        }
        Collection<PasField> result = new HashSet<PasField>();
        result.addAll(publicMembers.values());
        result.addAll(privateMembers.values());
        return result;
    }

    private void buildPrivateMembers() throws PasInvalidScopeException {
        if (isCacheActual(privateMembers, buildPrivateStamp)) { return; } // TODO: check correctness
        privateMembers = new LinkedHashMap<String, PasField>();
        redeclaredPrivateMembers = new LinkedHashSet<PascalNamedElement>();

        PsiElement section = PsiUtil.getModuleImplementationSection(this);
        if (null == section) section = this;
        if (!PsiUtil.checkeElement(section)) return;

        collectFields(section, privateMembers, redeclaredPrivateMembers);

        privateUnits = retrieveUsedUnits(section);
        for (PasEntityScope unit : privateUnits) {
            privateMembers.put(unit.getName().toUpperCase(), new PasField(this, unit, unit.getName(), PasField.FieldType.UNIT, PasField.Visibility.PRIVATE));
        }

        buildPrivateStamp = getContainingFile().getModificationStamp();
        //System.out.println(String.format("Unit %s private: %d, used: %d", getName(), privateMembers.size(), privateUnits != null ? privateUnits.size() : 0));
    }

    @SuppressWarnings("unchecked")
    private List<PasEntityScope> retrieveUsedUnits(PsiElement section) {
        List<PasEntityScope> result;
        List<PasNamespaceIdent> usedNames = PsiUtil.getUsedUnits(section);
        result = new ArrayList<PasEntityScope>(usedNames.size());
        for (PasNamespaceIdent ident : usedNames) {
            addUnit(result, PasReferenceUtil.findUnit(section.getProject(), ModuleUtilCore.findModuleForPsiElement(section), ident.getName()));
        }
        for (String unitName : PascalParserUtil.EXPLICIT_UNITS) {
            if (!unitName.equalsIgnoreCase(getName())) {
                addUnit(result, PasReferenceUtil.findUnit(section.getProject(), ModuleUtilCore.findModuleForPsiElement(section), unitName));
            }
        }
        return result;
    }

    private void addUnit(List<PasEntityScope> result, PasEntityScope unit) {
        if (unit != null) {
            result.add(unit);
        }
    }

    private void buildPublicMembers() throws PasInvalidScopeException {
        if (isCacheActual(publicMembers, buildPublicStamp)) { return; } // TODO: check correctness
        publicMembers = new LinkedHashMap<String, PasField>();
        redeclaredPublicMembers = new LinkedHashSet<PascalNamedElement>();

        publicMembers.put(getName().toUpperCase(), new PasField(this, this, getName(), PasField.FieldType.UNIT, PasField.Visibility.PUBLIC));

        PsiElement section = PsiUtil.getModuleInterfaceSection(this);
        if (null == section) return;
        if (!PsiUtil.checkeElement(section)) return;

        collectFields(section, publicMembers, redeclaredPublicMembers);

        publicUnits = retrieveUsedUnits(section);
        for (PasEntityScope unit : publicUnits) {
            publicMembers.put(unit.getName().toUpperCase(), new PasField(this, unit, unit.getName(), PasField.FieldType.UNIT, PasField.Visibility.PRIVATE));
        }

        buildPublicStamp = getContainingFile().getModificationStamp();
        //System.out.println(String.format("Unit %s public: %d, used: %d", getName(), publicMembers.size(), publicUnits != null ? publicUnits.size() : 0));
    }

    @SuppressWarnings("unchecked")
    private void collectFields(PsiElement section, final Map<String, PasField> members, final Set<PascalNamedElement> redeclaredMembers) {
        PsiUtil.processEntitiesInSection(this, section, PasField.Visibility.PRIVATE,
                new FieldCollector() {
                    @Override
                    public boolean fieldExists(PascalNamedElement element) {
                        PasField existing = members.get(element.getName().toUpperCase());
                        if (existing != null) {
                            if (!PsiUtil.isForwardClassDecl(existing.element)) {         // Otherwise replace with full declaration
                                redeclaredMembers.add(element);
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public void addField(String name, PasField field) {
                        PasField existing = members.get(field.name.toUpperCase());
                        if (existing != null) {
                            field.offset = existing.offset;
                        }
                        members.put(name.toUpperCase(), field);
                    }
                },
                PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class);
    }

    private boolean isCacheActual(Map<String, PasField> cache, long stamp) throws PasInvalidScopeException {
        if (!PsiUtil.checkeElement(this)) {
            return false;
        }
        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return false;
        }
        return (cache != null) && (getContainingFile().getModificationStamp() == stamp);
    }

    public List<PasEntityScope> getPrivateUnits() {
        return privateUnits;
    }

    public List<PasEntityScope> getPublicUnits() {
        return publicUnits;
    }

    @NotNull
    @Override
    public List<PasEntityScope> getParentScope() throws PasInvalidScopeException {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public PasEntityScope getNearestAffectingScope() throws PasInvalidScopeException {
        return null;
    }

    @Override
    synchronized public void invalidateCache() {
        System.out.println("*** invalidating cache");
        privateMembers = null;
        publicMembers = null;
    }

}
