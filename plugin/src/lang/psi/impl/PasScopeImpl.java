package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
public abstract class PasScopeImpl extends PascalNamedElementImpl implements PasEntityScope {

    protected static final Logger LOG = Logger.getInstance(PasScopeImpl.class.getName());

    volatile private String cachedUniqueName;
    volatile boolean building = false;
    volatile private String cachedKey;

    PasScopeImpl(ASTNode node) {
        super(node);
    }

    @Override
    protected PascalHelperNamed createHelper() {
        return new PascalHelperScope(this);
    }

    @Override
    public void invalidateCache(boolean subtreeChanged) {
        super.invalidateCache(subtreeChanged);
        cachedKey = null;
    }

    @Override
    public String getUniqueName() {
        String uniqueName = cachedUniqueName;
        if ((uniqueName == null) || (uniqueName.length() == 0)) {
            uniqueName = calcUniqueName();
        }
        cachedUniqueName = uniqueName;
        return uniqueName;
    }

    protected String calcUniqueName() {
        PasEntityScope scope = getContainingScope();
        return (scope != null ? scope.getUniqueName() + "." : "") + getName();
    }

    public final String getKey() {
        String key = cachedKey;
        if (null == key) {
            key = calcKey();
            cachedKey = key;
        }
        return key;
    }

    protected String calcKey() {
        PasEntityScope scope = this.getContainingScope();
        return String.format("%s%s", PsiUtil.getFieldName(this), scope != null ? "." + scope.getKey() : "");
    }

    <T extends PascalHelperScope.Cached> void ensureChache(Cache<String, T> cache) {
/*        if (!PsiUtil.checkeElement(this)) {
            return false;
        }*/
/*        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return false;
        }*/
        if (!PsiUtil.isElementValid(this)) {
            invalidateCaches(getKey());
            throw new ProcessCanceledException();
        }
        PascalHelperScope.Cached members = cache.getIfPresent(getKey());
        if ((members != null) && (getStamp(getContainingFile()) != members.stamp)) {
            invalidateCaches(getKey());
        }
    }

    void invalidateCaches(String key) {
        PascalModuleImpl.invalidate(key);
        PascalRoutineImpl.invalidate(key);
        PasStubStructTypeImpl.invalidate(key);
        cachedUniqueName = null;
        cachedKey = null;
    }

    static long getStamp(PsiFile file) {
        return file.getModificationStamp();
    }

    @Nullable
    @Override
    public PasEntityScope getContainingScope() {
        return getHelper().calcContainingScope();
    }

    private PascalHelperScope getHelper() {
        return (PascalHelperScope) helper;
    }

}
