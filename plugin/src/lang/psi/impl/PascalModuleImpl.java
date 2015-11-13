package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Author: George Bakhtadze
 * Date: 14/09/2013
 */
public class PascalModuleImpl extends PasScopeImpl implements PascalModule {

    private static final UnitMembers EMPTY_MEMBERS = new UnitMembers();
    private static final Cache<String, Members> privateCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private static final Cache<String, Members> publicCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    public PascalModuleImpl(ASTNode node) {
        super(node);
    }

    @Override
    public ModuleType getModuleType() {
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
        } catch (ExecutionException e) {
            LOG.error("Error occured during building private members", e.getCause());
            return EMPTY_MEMBERS;
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof ProcessCanceledException) {
                throw (ProcessCanceledException) e.getCause();
            } else {
                LOG.error("Unchecked error occured during building private members", e.getCause());
            }
            return EMPTY_MEMBERS;
        }
    }

    @Override
    @Nullable
    public final PasField getField(final String name) throws PasInvalidScopeException {
        PasField result = getPublicField(name);
        if (null == result) {
            result = getPrivateField(name);
        }
        return result;
    }

    @Override
    @Nullable
    synchronized public final PasField getPrivateField(final String name) throws PasInvalidScopeException {
        return getMembers(privateCache, this.new PrivateBuilder()).all.get(name.toUpperCase());
    }

    @Override
    @NotNull
    synchronized public Collection<PasField> getPrivateFields() throws PasInvalidScopeException {
        return getMembers(privateCache, this.new PrivateBuilder()).all.values();
    }

    @Override
    synchronized public List<PasEntityScope> getPrivateUnits() throws PasInvalidScopeException {
        return getMembers(privateCache, this.new PrivateBuilder()).units;
    }

    @Override
    @Nullable
    synchronized public final PasField getPublicField(final String name) throws PasInvalidScopeException {
        return getMembers(publicCache, this.new PublicBuilder()).all.get(name.toUpperCase());
    }

    @Override
    @NotNull
    synchronized public Collection<PasField> getPubicFields() throws PasInvalidScopeException {
        return getMembers(publicCache, this.new PublicBuilder()).all.values();
    }

    @Override
    synchronized public List<PasEntityScope> getPublicUnits() throws PasInvalidScopeException {
        return getMembers(publicCache, this.new PublicBuilder()).units;
    }

    public static void invalidate(String key) {
        privateCache.invalidate(key);
        publicCache.invalidate(key);
    }

    private class PrivateBuilder implements Callable<UnitMembers> {
        @Override
        public UnitMembers call() throws Exception {
            PsiElement section = PsiUtil.getModuleImplementationSection(PascalModuleImpl.this);
            if (null == section) section = PascalModuleImpl.this;
            UnitMembers res = new UnitMembers();
            if (!PsiUtil.checkeElement(section)) {
                //throw new PasInvalidElementException(section);
                return res;
            };

            collectFields(section, PasField.Visibility.PRIVATE, res.all, res.redeclared);

            res.units = retrieveUsedUnits(section);
            for (PasEntityScope unit : res.units) {
                res.all.put(unit.getName().toUpperCase(), new PasField(PascalModuleImpl.this, unit, unit.getName(), PasField.FieldType.UNIT, PasField.Visibility.PRIVATE));
            }

            res.stamp = getStamp(getContainingFile());
            LOG.info(String.format("Unit %s private: %d, used: %d", getName(), res.all.size(), res.units != null ? res.units.size() : 0));
            return res;
        }
    }

    private class PublicBuilder implements Callable<UnitMembers> {
        @Override
        public UnitMembers call() throws Exception {
            UnitMembers res = new UnitMembers();
            res.all.put(getName().toUpperCase(), new PasField(PascalModuleImpl.this, PascalModuleImpl.this, getName(), PasField.FieldType.UNIT, PasField.Visibility.PUBLIC));

            PsiElement section = PsiUtil.getModuleInterfaceSection(PascalModuleImpl.this);
            if (null == section) {
                //throw new PasInvalidElementException(section);
                return res;
            }
            if ((!PsiUtil.checkeElement(section))) {
                System.out.println("***");
                //throw new PasInvalidElementException(section);
                //return res;
            }

            collectFields(section, PasField.Visibility.PRIVATE, res.all, res.redeclared);

            res.units = retrieveUsedUnits(section);
            for (PasEntityScope unit : res.units) {
                res.all.put(unit.getName().toUpperCase(), new PasField(PascalModuleImpl.this, unit, unit.getName(), PasField.FieldType.UNIT, PasField.Visibility.PRIVATE));
            }

            res.stamp = getStamp(getContainingFile());
            LOG.info(String.format("Unit %s public: %d, used: %d", getName(), res.all.size(), res.units != null ? res.units.size() : 0));
            return res;
        }
    }

    @NotNull
    @Override
    synchronized public Collection<PasField> getAllFields() throws PasInvalidScopeException {
        if (!PsiUtil.checkeElement(this)) {
            invalidateCaches(getKey());
            //return Collections.emptyList();
        }
        Collection<PasField> result = new LinkedHashSet<PasField>();
        result.addAll(getPubicFields());
        result.addAll(getPrivateFields());
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<PasEntityScope> retrieveUsedUnits(PsiElement section) {
        List<PasEntityScope> result;
        List<PasNamespaceIdent> usedNames = PsiUtil.getUsedUnits(section);
        result = new ArrayList<PasEntityScope>(usedNames.size());
        List<VirtualFile> unitFiles = PasReferenceUtil.findUnitFiles(section.getProject(), ModuleUtilCore.findModuleForPsiElement(section));
        for (PasNamespaceIdent ident : usedNames) {
            addUnit(result, PasReferenceUtil.findUnit(section.getProject(), unitFiles, ident.getName()));
        }
        for (String unitName : PascalParserUtil.EXPLICIT_UNITS) {
            if (!unitName.equalsIgnoreCase(getName())) {
                addUnit(result, PasReferenceUtil.findUnit(section.getProject(), unitFiles, unitName));
            }
        }
        return result;
    }

    private void addUnit(List<PasEntityScope> result, PasEntityScope unit) {
        if (unit != null) {
            result.add(unit);
        }
    }

    private boolean isCacheActual(Map<String, PasField> cache, long stamp) throws PasInvalidScopeException {
        if (!PsiUtil.checkeElement(this)) {
            return false;
        }
        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return false;
        }
        return (cache != null) && (getStamp(getContainingFile()) == stamp);
        //return (cache != null) && (stamp > getStamp(getContainingFile()) - CACHE_LIVE_MS);
    }

    @NotNull
    @Override
    public List<PasEntityScope> getParentScope() throws PasInvalidScopeException {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public PasEntityScope getContainingScope() throws PasInvalidScopeException {
        return null;
    }

}
