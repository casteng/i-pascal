package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
public abstract class PasScopeImpl extends PascalNamedElementImpl implements PasEntityScope {

    protected static final Logger LOG = Logger.getInstance(PasScopeImpl.class.getName());
    static final Members EMPTY_MEMBERS = new Members();

    private String myCachedUniqueName;

    volatile boolean building = false;
    volatile String cachedKey;

    private ReentrantLock nameLock = new ReentrantLock();
    private ReentrantLock containingScopeLock = new ReentrantLock();

    volatile protected SmartPsiElementPointer<PasEntityScope> containingScope;

    public PasScopeImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String getUniqueName() {
        if (SyncUtil.lockOrCancel(nameLock)) {
            try {
                if ((myCachedUniqueName == null) || (myCachedUniqueName.length() == 0)) {
                    myCachedUniqueName = calcUniqueName();
                }
            } finally {
                nameLock.unlock();
            }
        }
        return myCachedUniqueName;
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

    <T extends Cached> void ensureChache(Cache<String, T> cache) {
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
        Cached members = cache.getIfPresent(getKey());
        if ((members != null) && (getStamp(getContainingFile()) != members.stamp)) {
            invalidateCaches(getKey());
        }
    }

    void invalidateCaches(String key) {
        PascalModuleImpl.invalidate(key);
        PascalRoutineImpl.invalidate(key);
        PasStubStructTypeImpl.invalidate(key);
        if (SyncUtil.lockOrCancel(nameLock)) {
            myCachedUniqueName = null;
            nameLock.unlock();
        }
        if (SyncUtil.lockOrCancel(containingScopeLock)) {
            containingScope = null;
            containingScopeLock.unlock();
        }
        cachedKey = null;
    }

    static long getStamp(PsiFile file) {
        //return System.currentTimeMillis();
        return file.getModificationStamp();
    }

    @SuppressWarnings("unchecked")
    void collectFields(PsiElement section, PasField.Visibility visibility,
                       final Map<String, PasField> members, final Set<PascalNamedElement> redeclaredMembers) {
        if (null == section) {
            return;
        }
        for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, PasNamedIdent.class, PasNamedIdentDeclImpl.class, PasGenericTypeIdent.class, PasNamespaceIdent.class, PasClassQualifiedIdent.class)) {
            if (PsiUtil.isSameAffectingScope(PsiUtil.getNearestAffectingDeclarationsRoot(namedElement), section)) {
                if (!PsiUtil.isFormalParameterName(namedElement) && !PsiUtil.isUsedUnitName(namedElement)) {
                    if (PsiUtil.isRoutineName(namedElement)) {
                        namedElement = (PascalNamedElement) namedElement.getParent();
                    }
                    String name = namedElement.getName();
                    String memberName = PsiUtil.getFieldName(namedElement).toUpperCase();
                    PasField existing = members.get(memberName);
                    if (shouldAddField(existing, namedElement)) {                       // Otherwise replace with full declaration
                        PasField field = addField(this, name, namedElement, visibility);
                        if ((existing != null) && (field.offset > existing.offset)) {
                            field.offset = existing.offset;               // replace field but keep offset to resolve fields declared later
                        }
                        if (field.fieldType == PasField.FieldType.ROUTINE) {
                            members.put(memberName, field);
                        }
                        members.put(name.toUpperCase(), field);
                    } else {
                        redeclaredMembers.add(namedElement);
                    }
                }
            }
        }
    }

    // Add forward declared field even if it exists as we need full declaration
    // Routines can have various signatures
    private static boolean shouldAddField(PasField existing, PascalNamedElement namedElement) {
        return (null == existing) || (PsiUtil.isForwardClassDecl(existing.getElement())
                || ((existing.fieldType == PasField.FieldType.ROUTINE) && (existing.offset > namedElement.getTextRange().getStartOffset())));
    }

    private static PasField addField(PasEntityScope owner, String name, PascalNamedElement namedElement, PasField.Visibility visibility) {
        PasField.FieldType fieldType = PsiUtil.getFieldType(namedElement);
        return new PasField(owner, namedElement, name, fieldType, visibility);
    }

    static class Cached {
        static final int UNCACHEABLE_STAMP = -1000000000;
        long stamp;
        public boolean isChachable() {
            return stamp != UNCACHEABLE_STAMP;
        }
    }

    static class Members extends Cached {
        Map<String, PasField> all = new LinkedHashMap<String, PasField>();
        Set<PascalNamedElement> redeclared = new LinkedHashSet<PascalNamedElement>();
        static Members createNotCacheable() {
            Members res = new Members();
            res.stamp = UNCACHEABLE_STAMP;
            return res;
        }
    }

    @Nullable
    @Override
    public PasEntityScope getContainingScope() {
        if (SyncUtil.tryLockQuiet(containingScopeLock, SyncUtil.LOCK_TIMEOUT_MS)) {
            try {
                if (!PsiUtil.isSmartPointerValid(containingScope)) {
                    calcContainingScope();
                }
                return containingScope != null ? containingScope.getElement() : null;
            } finally {
                containingScopeLock.unlock();
            }
        } else {
            return null;
        }
    }

    /**
     * 1. For methods and method implementations returns containing class
     * 2. For routines returns containing module
     * 3. For nested routines returns containing routine
     * 4. For structured types returns containing module
     * 5. For nested structured types returns containing type
     */
    private void calcContainingScope() {
        PasEntityScope scope = PsiUtil.getNearestAffectingScope(this);  // 2, 3, 4, 5, 1 for method declarations
        containingScope = SmartPointerManager.getInstance(scope.getProject()).createSmartPsiElementPointer(scope);
        if ((scope instanceof PascalModuleImpl) && (this instanceof PasRoutineImplDecl)) {            // 1 for method implementations
            String[] names = PsiUtil.getQualifiedMethodName(this).split("\\.");
            if (names.length <= 1) {
                return;
            }
            PasField field = scope.getField(PsiUtil.cleanGenericDef(names[0]));
            scope = updateContainingScope(field);
            for (int i = 1; (i < names.length - 1) && (scope != null); i++) {
                scope = updateContainingScope(scope.getField(PsiUtil.cleanGenericDef(names[i])));
            }
        }
    }

    private PasEntityScope updateContainingScope(PasField field) {
        if (null == field) {
            return null;
        }
        PasEntityScope scope = PasReferenceUtil.retrieveFieldTypeScope(field, new ResolveContext(field.owner, PasField.TYPES_TYPE,
                true, null, ModuleUtil.retrieveUnitNamespaces(field.owner)));
        if (scope != null) {
            containingScope = SmartPointerManager.getInstance(scope.getProject()).createSmartPsiElementPointer(scope);
        }
        return scope;
    }

}
