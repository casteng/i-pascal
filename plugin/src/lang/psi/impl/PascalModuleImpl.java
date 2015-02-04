package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.FieldCollector;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private Set<PascalNamedElement> redeclaredPublicMembers = null;
    private Set<PascalNamedElement> redeclaredPrivateMembers = null;
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

        //noinspection unchecked
        PsiUtil.retrieveEntitiesFromSection(this, PsiUtil.getModuleImplementationSection(this), PasField.Visibility.PRIVATE,
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
        System.out.println(getName() + ": buildPrivateMembers: " + privateMembers.size() + "members");
    }

    synchronized private void buildPublicMembers() {
        if (isCacheActual(publicMembers, buildPublicStamp)) { return; } // TODO: check correctness
        publicMembers = new LinkedHashMap<String, PasField>();
        redeclaredPublicMembers = new LinkedHashSet<PascalNamedElement>();
        System.out.println("module buildPublicMembers: " + getName());
        //noinspection unchecked
        PsiUtil.retrieveEntitiesFromSection(this, PsiUtil.getModuleImplementationSection(this), PasField.Visibility.PUBLIC,
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
        System.out.println(getName() + ": buildPublicMembers: " + publicMembers.size() + "members");
    }

    public boolean isCacheActual(Map<String, PasField> cache, long stamp) {
        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return false;
        }
        return (cache != null) && (getContainingFile().getModificationStamp() == stamp);
    }
}
