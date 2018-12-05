package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PasModuleStub;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: George Bakhtadze
 * Date: 14/09/2013
 */
public abstract class PascalModuleImpl extends PasStubScopeImpl<PasModuleStub> implements PascalModule {

    private static final UnitMembers EMPTY_MEMBERS = new UnitMembers();
    private static final Idents EMPTY_IDENTS = new Idents();
    private static final Cache<String, Members> privateCache = CacheBuilder.newBuilder().softValues().build();
    private static final Cache<String, Members> publicCache = CacheBuilder.newBuilder().softValues().build();
    private static final Cache<String, Idents> identCache = CacheBuilder.newBuilder().softValues().build();

    private final Callable<? extends Members> PRIVATE_BUILDER = this.new PrivateBuilder();
    private final Callable<? extends Members> PUBLIC_BUILDER = this.new PublicBuilder();
    private final Callable<Idents> IDENTS_BUILDER = this.new IdentsBuilder();

    private Set<String> usedUnitsPublic = null;
    private Set<String> usedUnitsPrivate = null;
    private List<SmartPsiElementPointer<PasEntityScope>> privateUnits = null;
    private List<SmartPsiElementPointer<PasEntityScope>> publicUnits = null;
    private ReentrantLock unitsLock = new ReentrantLock();
    private ReentrantLock publicUnitsLock = new ReentrantLock();
    private ReentrantLock privateUnitsLock = new ReentrantLock();
    volatile private Collection<PasWithStatement> withStatements;

    public PascalModuleImpl(ASTNode node) {
        super(node);
    }

    public PascalModuleImpl(@NotNull PasModuleStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    protected boolean calcIsExported() {
        return true;
    }

    @Override
    protected String calcUniqueName() {
        String result = getName();
        return StringUtils.isNotEmpty(result) ? result : getContainingFile().getName();
    }

    @Override
    protected String calcKey() {
        return PsiUtil.getContainingFilePath(this);
    }

    @Override
    public ModuleType getModuleType() {
        PasModuleStub stub = retrieveStub();
        if (stub != null) {
            return stub.getModuleType();
        } else {
            return resolveModuleType();
        }
    }

    private ModuleType resolveModuleType() {
        PasModule pm = (PasModule) this;
        if (pm.getUnitModuleHead() != null) {
            return ModuleType.UNIT;
        } else if (pm.getLibraryModuleHead() != null) {
            return ModuleType.LIBRARY;
        } else if (pm.getPackageModuleHead() != null) {
            return ModuleType.PACKAGE;
        }
        return ModuleType.PROGRAM;
    }

    @NotNull
    private UnitMembers getMembers(Cache<String, Members> cache, Callable<? extends Members> builder) {
        ensureChache(cache);
        try {
            return (UnitMembers) cache.get(getKey(), builder);
        } catch (Exception e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.warn("Error occured during building members for: " + this, e.getCause());
            }
            invalidateCaches();
            return EMPTY_MEMBERS;
        }
    }

    @Override
    @Nullable
    public final PasField getField(final String name) {
        if (retrieveStub() != null) {
            return getFieldStub(name);
        } else {
            PasField result = getPublicField(name);
            if (null == result) {
                result = getPrivateField(name);
            }
            return result;
        }
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        if (retrieveStub() != null) {
            return getAllFieldsStub();
        } else {
            if (!PsiUtil.checkeElement(this)) {
                invalidateCaches();
            }
            Collection<PasField> result = new LinkedHashSet<PasField>();
            result.addAll(getPubicFields());
            result.addAll(getPrivateFields());
            return result;
        }
    }

    PasField getFieldStub(String name) {
        PasField result = super.getFieldStub(name);
        if ((null == result) && getName().equalsIgnoreCase(name)) {
            result = new PasField(this.retrieveStub(), null);       // This unit name reference
        }
        return result;
    }

    Collection<PasField> getAllFieldsStub() {
        Collection<PasField> res = super.getAllFieldsStub();
        res.add(new PasField(this.retrieveStub(), null));       // This unit name reference
        return res;
    }

    @Override
    @Nullable
    public final PasField getPrivateField(final String name) {
        return getPasField(name, privateCache, PRIVATE_BUILDER);
    }

    @Override
    @NotNull
    public Collection<PasField> getPrivateFields() {
        return getMembers(privateCache, PRIVATE_BUILDER).all.values();
    }

    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getPrivateUnits() {
        if (SyncUtil.lockOrCancel(privateUnitsLock)) {
            try {
                if (null == privateUnits) {
                    Set<String> units = getUsedUnitsPrivate();
                    privateUnits = new ArrayList<>(units.size() + PascalParserUtil.EXPLICIT_UNITS.size());
                    addUnits(privateUnits, units);
                    addUnits(privateUnits, PascalParserUtil.EXPLICIT_UNITS);
                }
            } finally {
                privateUnitsLock.unlock();
            }
        }
        return privateUnits;
    }

    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getPublicUnits() {
        if (SyncUtil.lockOrCancel(publicUnitsLock)) {
            try {
                if (null == publicUnits) {
                    Set<String> units = getUsedUnitsPublic();
                    publicUnits = new ArrayList<>(units.size() + PascalParserUtil.EXPLICIT_UNITS.size());
                    addUnits(publicUnits, units);
                    addUnits(publicUnits, PascalParserUtil.EXPLICIT_UNITS);
                }
            } finally {
                publicUnitsLock.unlock();
            }
        }
        return publicUnits;
    }

    private void addUnits(List<SmartPsiElementPointer<PasEntityScope>> result, Collection<String> units) {
        Project project = getProject();
        for (String unitName : units) {
            for (PascalModule pascalModule : ResolveUtil.findUnitsWithStub(getProject(), ModuleUtilCore.findModuleForPsiElement(this), unitName)) {
                result.add(SmartPointerManager.getInstance(project).createSmartPsiElementPointer(pascalModule));
            }
        }
    }

    @Override
    @Nullable
    public final PasField getPublicField(final String name) {
        return getPasField(name, publicCache, PUBLIC_BUILDER);
    }

    @Override
    @NotNull
    public Collection<PasField> getPubicFields() {
        return getMembers(publicCache, PUBLIC_BUILDER).all.values();
    }

    @NotNull
    private Idents getIdents(Cache<String, Idents> cache, Callable<? extends Idents> builder) {
        ensureChache(cache);
        try {
            return cache.get(getKey(), builder);
        } catch (Exception e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.warn("Error occured during building idents for: " + this, e.getCause());
            }
            invalidateCaches();
            return EMPTY_IDENTS;
        }
    }

    @Override
    public Pair<List<PascalNamedElement>, List<PascalNamedElement>> getIdentsFrom(@Nullable String module, boolean includeInterface, List<String> unitPrefixes) {
        Idents idents = getIdents(identCache, IDENTS_BUILDER);
        Pair<List<PascalNamedElement>, List<PascalNamedElement>> res = new Pair<List<PascalNamedElement>, List<PascalNamedElement>>(
                new SmartList<PascalNamedElement>(), new SmartList<PascalNamedElement>());
        if (includeInterface) {
            collectElements(module, idents.identsIntf, res.first, unitPrefixes);
        }
        collectElements(module, idents.identsImpl, res.second, unitPrefixes);
        return res;
    }

    private PasField getPasField(String name, Cache<String, Members> cache, Callable<? extends Members> builder) {
        PasField res = getMembers(cache, builder).all.get(name.toUpperCase());
        if ((res != null) && !PsiUtil.isElementUsable(res.getElement())) {
            LOG.info(String.format("WARN: element for name %s in %s is invalid. Clearing caches.", name, getUniqueName()));
            invalidateCaches();
            return getMembers(cache, builder).all.get(name.toUpperCase());
        } else {
            return res;
        }
    }

    private void collectElements(@Nullable String module, Map<PascalNamedElement, PasField> idents, List<PascalNamedElement> result, List<String> unitPrefixes) {
        for (Map.Entry<PascalNamedElement, PasField> entry : idents.entrySet()) {
            PasField field = entry.getValue();
            if ((field != null) && PasField.isAllowed(field.visibility, PasField.Visibility.PRIVATE)
                    && PasField.TYPES_STRUCTURE.contains(field.fieldType)
                    && (field.owner instanceof PascalModule) && !this.equals(field.owner) && nameMatch(module, field.owner.getName(), unitPrefixes)) {
                result.add(entry.getKey());
            }
        }
    }

    private static boolean nameMatch(@Nullable String moduleName, String fieldName, List<String> unitPrefixes) {
        if ((null == moduleName) || moduleName.equalsIgnoreCase(fieldName)) {
            return true;
        }
        if (moduleName.indexOf('.') >= 0) {
            return false;
        }
        moduleName = moduleName.toUpperCase();
        fieldName = fieldName.toUpperCase();
        for (String prefix : unitPrefixes) {
            String prefixed = prefix.toUpperCase() + "." + moduleName;
            if (prefixed.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Set<String> getUsedUnitsPublic() {
        PasModuleStub stub = retrieveStub();
        if (stub != null) {
            return stub.getUsedUnitsPublic();
        }
        if (SyncUtil.lockOrCancel(unitsLock)) {
            try {
                if (null == usedUnitsPublic) {
                    usedUnitsPublic = new SmartHashSet<>();
                    for (PascalQualifiedIdent ident : PsiUtil.getUsedUnits(PsiUtil.getModuleInterfaceSection(this))) {
                        usedUnitsPublic.add(ident.getName());
                    }
                }
            } finally {
                unitsLock.unlock();
            }
        }
        return usedUnitsPublic;
    }

    @NotNull
    @Override
    public Set<String> getUsedUnitsPrivate() {
        PasModuleStub stub = retrieveStub();
        if (stub != null) {
            return stub.getUsedUnitsPrivate();
        }
        if (SyncUtil.lockOrCancel(unitsLock)) {
            try {
                if (null == usedUnitsPrivate) {
                    usedUnitsPrivate = new SmartHashSet<>();
                    for (PascalQualifiedIdent ident : PsiUtil.getUsedUnits(PsiUtil.getModuleImplementationSection(this))) {
                        usedUnitsPrivate.add(ident.getName());
                    }
                }
            } finally {
                unitsLock.unlock();
            }
        }
        return usedUnitsPrivate;
    }

    @Override
    public void invalidateCaches() {
        super.invalidateCaches();
        if (SyncUtil.lockOrCancel(unitsLock)) {
            usedUnitsPrivate = null;
            usedUnitsPublic = null;
            unitsLock.unlock();
        }
        if (SyncUtil.lockOrCancel(privateUnitsLock)) {
            privateUnits = null;
            privateUnitsLock.unlock();
        }
        if (SyncUtil.lockOrCancel(publicUnitsLock)) {
            publicUnits = null;
            publicUnitsLock.unlock();
        }
        withStatements = null;
    }

    public static void invalidate(String key) {
        privateCache.invalidate(key);
        publicCache.invalidate(key);
        identCache.invalidate(key);
    }

    private static class Idents extends Cached {
        Map<PascalNamedElement, PasField> identsIntf = new HashMap<PascalNamedElement, PasField>();
        Map<PascalNamedElement, PasField> identsImpl = new HashMap<PascalNamedElement, PasField>();
    }

    private class IdentsBuilder implements Callable<Idents> {
        @Override
        public Idents call() throws Exception {
            Idents res = new Idents();
            //noinspection unchecked
            for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(PascalModuleImpl.this, PasSubIdentImpl.class, PasRefNamedIdentImpl.class)) {
                if (!PsiUtil.isLastPartOfMethodImplName(namedElement)) {
                    Collection<PasField> refs = PasReferenceUtil.resolveExpr(NamespaceRec.fromElement(namedElement), new ResolveContext(PasField.TYPES_ALL, true), 0);
                    if (!refs.isEmpty()) {
                        if (ContextUtil.belongsToInterface(namedElement)) {
                            res.identsIntf.put(namedElement, refs.iterator().next());
                        } else {
                            res.identsImpl.put(namedElement, refs.iterator().next());
                        }
                    }
                }
            }
            res.stamp = getStamp(getContainingFile());
            return res;
        }
    }

    private class PrivateBuilder implements Callable<UnitMembers> {
        @Override
        public UnitMembers call() throws Exception {
            PsiElement section = PsiUtil.getModuleImplementationSection(PascalModuleImpl.this);
            UnitMembers res = new UnitMembers();
            if (!PsiUtil.checkeElement(section)) {
                //throw new PasInvalidElementException(section);
                return res;
            }

            collectFields(section, PasField.Visibility.PRIVATE, res.all, res.redeclared);

            for (SmartPsiElementPointer<PasEntityScope> unitPtr : PascalModuleImpl.this.getPrivateUnits()) {
                PasEntityScope unit = unitPtr.getElement();
                if (unit != null) {
                    res.all.put(unit.getName().toUpperCase(), new PasField(PascalModuleImpl.this, unit, unit.getName(), PasField.FieldType.UNIT, PasField.Visibility.PRIVATE));
                }
            }

            res.stamp = getStamp(getContainingFile());
            LOG.debug(String.format("Unit %s private: %d", getName(), res.all.size()));
            return res;
        }
    }

    private class PublicBuilder implements Callable<UnitMembers> {
        @Override
        public UnitMembers call() throws Exception {
            UnitMembers res = new UnitMembers();
            res.all.put(getName().toUpperCase(), new PasField(PascalModuleImpl.this, PascalModuleImpl.this, getName(), PasField.FieldType.UNIT, PasField.Visibility.PRIVATE));
            res.stamp = getStamp(getContainingFile());

            PsiElement section = PsiUtil.getModuleInterfaceSection(PascalModuleImpl.this);
            if (null == section) {
                //throw new PasInvalidElementException(section);
                return res;
            }
            if ((!PsiUtil.checkeElement(section))) {
                //throw new PasInvalidElementException(section);
                //return res;
            }

            collectFields(section, PasField.Visibility.PUBLIC, res.all, res.redeclared);

            for (SmartPsiElementPointer<PasEntityScope> unitPtr : PascalModuleImpl.this.getPublicUnits()) {
                PasEntityScope unit = unitPtr.getElement();
                if (unit != null) {
                    res.all.put(unit.getName().toUpperCase(), new PasField(PascalModuleImpl.this, unit, unit.getName(), PasField.FieldType.UNIT, PasField.Visibility.PRIVATE));
                }
            }

            LOG.debug(String.format("Unit %s public: %d", getName(), res.all.size()));
            return res;
        }
    }

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public PasEntityScope getContainingScope() {
        return null;
    }

    @NotNull
    @Override
    public Collection<PasWithStatement> getWithStatements() {
        if (null == withStatements) {
            PsiElement base;
            switch (getModuleType()) {
                case UNIT:
                    base = ((PasModule) this).getUnitImplementation();
                    break;
                case PROGRAM:
                case LIBRARY:
                    PasBlockGlobal blockGlobal = ((PasModule) this).getBlockGlobal();
                    base = blockGlobal != null ? blockGlobal.getBlockBody() : this;
                    break;
                default:
                    base = null;
            }
            withStatements = base != null ? PsiTreeUtil.findChildrenOfType(base, PasWithStatement.class) : Collections.emptyList();
        }
        return withStatements;
    }
}
