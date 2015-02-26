package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
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
    private long buildPrivateStamp = 0;
    private long buildPublicStamp = 0;

    public PascalModuleImpl(ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public final PasField getField(final String name) {
        if (publicMembers == null) {
            buildPublicMembers();           // TODO: clarify order of members iteration
        }
        PasField result = publicMembers.get(name);
        if (null == result) {
            if (privateMembers == null) {
                buildPrivateMembers();
            }
            result = privateMembers.get(name);
        }
        return result;
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
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

    synchronized private void buildPrivateMembers() {
        if (isCacheActual(privateMembers, buildPrivateStamp)) { return; } // TODO: check correctness
        privateMembers = new LinkedHashMap<String, PasField>();
        redeclaredPrivateMembers = new LinkedHashSet<PascalNamedElement>();

        PsiElement section = PsiUtil.getModuleImplementationSection(this);
        //noinspection unchecked
        PsiUtil.retrieveEntitiesFromSection(this, section, PasField.Visibility.PRIVATE,
                new FieldCollector() {
                    @Override
                    public boolean fieldExists(PascalNamedElement element) {
                        if (privateMembers.containsKey(element.getName())) {
                            redeclaredPrivateMembers.add(element);
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void addField(String name, PasField field) {
                        privateMembers.put(name, field);
                    }
                },
                PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class);

        privateUnits = retrieveUsedUnits(section);

        buildPrivateStamp = getContainingFile().getModificationStamp();
        System.out.println(String.format("Unit %s private: %d, used: %d", getName(), privateMembers.size(), privateUnits != null ? privateUnits.size() : 0));
    }

    @SuppressWarnings("unchecked")
    private List<PasEntityScope> retrieveUsedUnits(PsiElement section) {
        List<PasEntityScope> result;
        List<PasNamespaceIdent> usedNames = PsiUtil.getUsedUnits(section);
        result = new ArrayList<PasEntityScope>(usedNames.size());
        for (PasNamespaceIdent ident : usedNames) {
            result.add(PasReferenceUtil.findUnit(section.getProject(), ModuleUtilCore.findModuleForPsiElement(section), ident.getName()));
        }
        for (String unitName : PascalParserUtil.EXPLICIT_UNITS) {
            result.add(PasReferenceUtil.findUnit(section.getProject(), ModuleUtilCore.findModuleForPsiElement(section), unitName));
        }
        return result;
    }

    synchronized private void buildPublicMembers() {
        if (isCacheActual(publicMembers, buildPublicStamp)) { return; } // TODO: check correctness
        publicMembers = new LinkedHashMap<String, PasField>();
        redeclaredPublicMembers = new LinkedHashSet<PascalNamedElement>();
        PsiElement section = PsiUtil.getModuleInterfaceSection(this);
        //noinspection unchecked
        PsiUtil.retrieveEntitiesFromSection(this, PsiUtil.getModuleInterfaceSection(this), PasField.Visibility.PUBLIC,
                new FieldCollector() {
                    @Override
                    public boolean fieldExists(PascalNamedElement element) {
                        if (publicMembers.containsKey(element.getName())) {
                            redeclaredPublicMembers.add(element);
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void addField(String name, PasField field) {
                        publicMembers.put(name, field);
                    }
                },
                PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class);
        publicUnits = retrieveUsedUnits(section);
        buildPublicStamp = getContainingFile().getModificationStamp();
        System.out.println(String.format("Unit %s public: %d, used: %d", getName(), publicMembers.size(), publicUnits != null ? publicUnits.size() : 0));
    }

    public boolean isCacheActual(Map<String, PasField> cache, long stamp) {
        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return false;
        }
        return (cache != null) && (getContainingFile().getModificationStamp() == stamp);
    }

    @Nullable
    @Override
    public PasFullyQualifiedIdent getParentScope() {
        return null;  // TODO: return used units (separate interface/implementation?)
    }

    public List<PasEntityScope> getPrivateUnits() {
        return privateUnits;
    }

    public List<PasEntityScope> getPublicUnits() {
        return publicUnits;
    }
}
