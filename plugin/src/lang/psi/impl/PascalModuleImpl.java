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
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.references.resolve.Resolve;
import com.siberika.idea.pascal.lang.references.resolve.ResolveProcessor;
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
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: George Bakhtadze
 * Date: 14/09/2013
 */
public abstract class PascalModuleImpl extends PasStubScopeImpl<PasModuleStub> implements PascalModule {

    private static final PascalHelperScope.UnitMembers EMPTY_MEMBERS = new PascalHelperScope.UnitMembers();
    private static final Idents EMPTY_IDENTS = new Idents();
    private static final Cache<String, PascalHelperScope.Members> privateCache = CacheBuilder.newBuilder().softValues().build();
    private static final Cache<String, PascalHelperScope.Members> publicCache = CacheBuilder.newBuilder().softValues().build();
    private static final Cache<String, Idents> identCache = CacheBuilder.newBuilder().softValues().build();

    private final Callable<? extends PascalHelperScope.Members> PRIVATE_BUILDER = this.new PrivateBuilder();
    private final Callable<? extends PascalHelperScope.Members> PUBLIC_BUILDER = this.new PublicBuilder();
    private final Callable<Idents> IDENTS_BUILDER = this.new IdentsBuilder();

    private List<String> usedUnitsPublic = null;
    private List<String> usedUnitsPrivate = null;
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
    public void invalidateCache(boolean subtreeChanged) {
        super.invalidateCache(subtreeChanged);
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
    private PascalHelperScope.UnitMembers getMembers(Cache<String, PascalHelperScope.Members> cache, Callable<? extends PascalHelperScope.Members> builder) {
        ensureChache(cache);
        try {
            return (PascalHelperScope.UnitMembers) cache.get(getKey(), builder);
        } catch (Exception e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.warn("Error occured during building members for: " + this, e.getCause());
            }
            invalidateCache(false);
            return EMPTY_MEMBERS;
        }
    }

    @Override
    @Nullable
    public final PasField getField(final String name) {
        if ((name != null) && (retrieveStub() != null)) {
            return getFieldStub(name);
        } else {
            PasField result = getPublicField(name);
            if (null == result) {
                result = getPrivateField(name);
            }
            return result;
        }
    }

    @Nullable
    @Override
    public PascalRoutine getRoutine(String reducedName) {
        return RoutineUtil.findRoutine(getAllFields(), reducedName);
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        if (retrieveStub() != null) {
            return getAllFieldsStub();
        } else {
            if (!PsiUtil.checkeElement(this)) {
                invalidateCache(false);
            }
            Collection<PasField> result = new LinkedHashSet<>();
            result.addAll(getPublicFields());
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
        if (null == privateUnits) {
            List<SmartPsiElementPointer<PasEntityScope>> result = doCollectUnits(getUsedUnitsPrivate(), getModuleType() != ModuleType.UNIT);
            if (SyncUtil.lockOrCancel(privateUnitsLock)) {
                privateUnits = result;
                privateUnitsLock.unlock();
            }
        }
        return privateUnits;
    }

    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getPublicUnits() {
        if (null == publicUnits) {
            List<SmartPsiElementPointer<PasEntityScope>> result = doCollectUnits(getUsedUnitsPublic(), true);
            if (SyncUtil.lockOrCancel(publicUnitsLock)) {
                publicUnits = result;
                publicUnitsLock.unlock();
            }
        }
        return publicUnits;
    }

    private List<SmartPsiElementPointer<PasEntityScope>> doCollectUnits(Collection<String> units, boolean addExplicit) {
        List<SmartPsiElementPointer<PasEntityScope>> result = new ArrayList<>(units.size() + PascalParserUtil.EXPLICIT_UNITS.size());
        addUnits(result, units);
        if (addExplicit) {
            addUnits(result, PascalParserUtil.EXPLICIT_UNITS);
        }
        return result;
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
    public Collection<PasField> getPublicFields() {
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
                LOG.warn("Error occurred during building idents for: " + this, e.getCause());
            }
            invalidateCache(false);
            return EMPTY_IDENTS;
        }
    }

    @Override
    public Pair<List<PascalNamedElement>, List<PascalNamedElement>> getIdentsFrom(@Nullable String module, boolean includeInterface, List<String> unitPrefixes) {
        Idents idents = getIdents(identCache, IDENTS_BUILDER);
        Pair<List<PascalNamedElement>, List<PascalNamedElement>> res = new Pair<List<PascalNamedElement>, List<PascalNamedElement>>(new SmartList<>(), new SmartList<>());
        if (includeInterface) {
            collectElements(module, idents.identsIntf, res.first, unitPrefixes);
        }
        collectElements(module, idents.identsImpl, res.second, unitPrefixes);
        return res;
    }

    private PasField getPasField(String name, Cache<String, PascalHelperScope.Members> cache, Callable<? extends PascalHelperScope.Members> builder) {
        PasField res = name != null ? getMembers(cache, builder).all.get(name.toUpperCase()) : null;
        if ((res != null) && !PsiUtil.isElementUsable(res.getElement())) {
            LOG.info(String.format("WARN: element for name %s in %s is invalid. Clearing caches.", name, getUniqueName()));
            invalidateCache(false);
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
                    && (field.owner instanceof PascalModule) && !this.equals(field.owner) && PsiUtil.isElementValid(field.owner) && nameMatch(module, field.owner.getName(), unitPrefixes)) {
                result.add(entry.getKey());
            }
        }
    }

    private static boolean nameMatch(@Nullable String moduleName, String fieldName, List<String> unitPrefixes) {
        if ((null == moduleName) || moduleName.equalsIgnoreCase(fieldName)) {
            return true;
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
    public List<String> getUsedUnitsPublic() {
        PasModuleStub stub = retrieveStub();
        if (stub != null) {
            return stub.getUsedUnitsPublic();
        }
        if (null == usedUnitsPublic) {
            List<String> result = new SmartList<>();
            for (PascalQualifiedIdent ident : PsiUtil.getUsedUnits(PsiUtil.getModuleInterfaceUsesClause(this))) {
                result.add(ident.getName());
            }
            if (SyncUtil.lockOrCancel(unitsLock)) {
                usedUnitsPublic = result;
                unitsLock.unlock();
            }
        }
        return usedUnitsPublic;
    }

    @NotNull
    @Override
    public List<String> getUsedUnitsPrivate() {
        PasModuleStub stub = retrieveStub();
        if (stub != null) {
            return stub.getUsedUnitsPrivate();
        }
        if (null == usedUnitsPrivate) {
            List<String> result = new SmartList<>();
            for (PascalQualifiedIdent ident : PsiUtil.getUsedUnits(PsiUtil.getModuleImplementationUsesClause(this))) {
                result.add(ident.getName());
            }
            if (SyncUtil.lockOrCancel(unitsLock)) {
                usedUnitsPrivate = result;
                unitsLock.unlock();
            }
        }
        return usedUnitsPrivate;
    }

    @Nullable
    @Override
    public PascalRoutine getPublicRoutine(String reducedName) {
        return RoutineUtil.findRoutine(getPublicFields(), reducedName);
    }

    @Nullable
    @Override
    public PascalRoutine getPrivateRoutine(String reducedName) {
        return RoutineUtil.findRoutine(getPrivateFields(), reducedName);
    }

    private static class Idents extends PascalHelperScope.Cached {
        Map<PascalNamedElement, PasField> identsIntf = new HashMap<>();
        Map<PascalNamedElement, PasField> identsImpl = new HashMap<>();
    }

    private class IdentsBuilder implements Callable<Idents> {
        @Override
        public Idents call() {
            Idents res = new Idents();
            final PsiElement intf = PsiUtil.getModuleInterfaceSection(PascalModuleImpl.this);
            if (intf != null) {
                collectIdents(intf, res.identsIntf);
            }
            final PsiElement impl = PsiUtil.getModuleImplementationSection(PascalModuleImpl.this);
            collectIdents(impl, res.identsImpl);
            res.stamp = getStamp(getContainingFile());
            return res;
        }
    }

    private void collectIdents(final PsiElement section, final Map<PascalNamedElement, PasField> identsMap) {
        //noinspection unchecked
        for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, PasSubIdentImpl.class, PasRefNamedIdentImpl.class)) {
            if (!PsiUtil.isLastPartOfMethodImplName(namedElement)) {
                Resolve.resolveExpr(NamespaceRec.fromElement(namedElement), new ResolveContext(PasField.TYPES_ALL, true),
                        new ResolveProcessor() {
                            @Override
                            public boolean process(final PasEntityScope originalScope, final PasEntityScope scope, final PasField field, final PasField.FieldType type) {
                                identsMap.put(namedElement, field);
                                return false;
                            }
                        }
                );
            }
        }
    }

    private class PrivateBuilder implements Callable<PascalHelperScope.UnitMembers> {
        @Override
        public PascalHelperScope.UnitMembers call() {
            PsiElement section = PsiUtil.getModuleImplementationSection(PascalModuleImpl.this);
            PascalHelperScope.UnitMembers res = new PascalHelperScope.UnitMembers();
            if (!PsiUtil.checkeElement(section)) {
                //throw new PasInvalidElementException(section);
                return res;
            }

            collectFields(section, PasField.Visibility.PRIVATE, res.all, res.redeclared);

            res.stamp = getStamp(getContainingFile());
            LOG.debug(String.format("Unit %s private: %d", getName(), res.all.size()));
            return res;
        }
    }

    private class PublicBuilder implements Callable<PascalHelperScope.UnitMembers> {
        @Override
        public PascalHelperScope.UnitMembers call() {
            PascalHelperScope.UnitMembers res = new PascalHelperScope.UnitMembers();
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
