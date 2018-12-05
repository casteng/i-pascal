package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.stub.PasIdentStubImpl;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import com.siberika.idea.pascal.lang.stub.struct.PasStructStub;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
@SuppressWarnings("unchecked")
public abstract class PasStubScopeImpl<B extends PasNamedStub> extends PascalNamedStubElement<B> implements PasEntityScope {

    protected static final Logger LOG = Logger.getInstance(PasStubScopeImpl.class.getName());

    protected static final Members EMPTY_MEMBERS = new Members();

    volatile protected String cachedKey;
    private Map<String, PasNamedStub> fieldsMap = new ConcurrentHashMap<>();
    private static final String KEY_EMPTY_MARKER = "#";

    private ReentrantLock containingScopeLock = new ReentrantLock();

    SmartPsiElementPointer<PasEntityScope> containingScope;

    public PasStubScopeImpl(ASTNode node) {
        super(node);
    }

    public PasStubScopeImpl(final B stub, IStubElementType noteType) {
        super(stub, noteType);
    }

    @Nullable
    @Override
    public B retrieveStub() {
        B stub = super.getStub();
        return stub;// != null ? stub : getGreenStub();
    }

    @Override
    protected String calcUniqueName() {
        PasEntityScope scope = getContainingScope();
        return calcScopeUniqueName(scope) + "." + getName();
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

    PasField getFieldStub(String name) {
        if (fieldsMap.containsKey(KEY_EMPTY_MARKER)) {
            PasNamedStub stub = fieldsMap.get(name.toUpperCase());
            return stub != null ? new PasField(stub, name) : null;
        }

        PasField result = null;
        List<StubElement> childrenStubs = retrieveStub().getChildrenStubs();
        for (StubElement stubElement : childrenStubs) {
            //String stubName = stubElement instanceof PasExportedRoutineStub ? ((PasExportedRoutineStub) stubElement).getCanonicalName() : ((PasNamedStub) stubElement).getName();
            String stubName = ((PasNamedStub) stubElement).getName();
            if (name.equalsIgnoreCase(stubName)) {
                result = new PasField((PasNamedStub) stubElement, null);
            }
            fieldsMap.put(stubName.toUpperCase(), (PasNamedStub) stubElement);
            if (stubElement instanceof PasStructStub) {
                List<String> aliases = ((PasStructStub) stubElement).getAliases();
                if (aliases != null) {
                    for (String alias : aliases) {
                        if (name.equalsIgnoreCase(alias)) {
                            result = new PasField((PasNamedStub) stubElement, alias);
                        }
                        fieldsMap.put(alias.toUpperCase(), (PasNamedStub) stubElement);
                    }
                }
            }
        }
        fieldsMap.put(KEY_EMPTY_MARKER, new PasIdentStubImpl(null, "", false, "", PasField.FieldType.VARIABLE, "", null, PasField.Access.READONLY, null, null));
        return result;
    }

    Collection<PasField> getAllFieldsStub() {
        Collection<PasField> res = new SmartList<>();
        List<StubElement> childrenStubs = retrieveStub().getChildrenStubs();
        for (StubElement stubElement : childrenStubs) {
            res.add(new PasField((PasNamedStub) stubElement, null));
            if (stubElement instanceof PasStructStub) {
                List<String> aliases = ((PasStructStub) stubElement).getAliases();
                if (aliases != null) {
                    for (String alias : aliases) {
                        res.add(new PasField((PasNamedStub) stubElement, alias));
                    }
                }
            }
        }
        return res;
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
            invalidateCaches();
            throw new ProcessCanceledException();
        }
        Cached members = cache.getIfPresent(getKey());
        if ((members != null) && (getStamp(getContainingFile()) != members.stamp)) {
            invalidateCaches();
        }
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        invalidateCaches();
    }

    @Override
    public void invalidateCaches() {
        if (cachedKey != null) {
            String key = cachedKey;
            PascalModuleImpl.invalidate(key);
            PascalRoutineImpl.invalidate(key);
            PasStubStructTypeImpl.invalidate(key);
            cachedKey = null;
        }
        if (SyncUtil.lockOrCancel(containingScopeLock)) {
            containingScope = null;
            containingScopeLock.unlock();
        }
        fieldsMap.clear();                                     // TODO: probably not safe
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
        PasField.FieldType fieldType = getFieldType(namedElement);
        return new PasField(owner, namedElement, name, fieldType, visibility);
    }

    private static PasField.FieldType getFieldType(PascalNamedElement namedElement) {
        PasField.FieldType type = PasField.FieldType.VARIABLE;
        if (PsiUtil.isTypeName(namedElement)) {
            type = PasField.FieldType.TYPE;
        } else if (namedElement instanceof PascalRoutine) {
            type = PasField.FieldType.ROUTINE;
        } else if (ContextUtil.isConstDecl(namedElement) || ContextUtil.isEnumDecl(namedElement)) {
            type = PasField.FieldType.CONSTANT;
        }

        return type;
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

    static class UnitMembers extends Members {
    }

    @Nullable
    @Override
    public PasEntityScope getContainingScope() {
        B stub = retrieveStub();
        if (stub != null) {
            StubElement parentStub = stub.getParentStub();
            PsiElement parent = parentStub != null ? parentStub.getPsi() : null;
            if (parent instanceof PasEntityScope) {
                return (PasEntityScope) parent;
            }
        }
        if (SyncUtil.lockOrCancel(containingScopeLock)) {
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
    void calcContainingScope() {
        PasEntityScope scope = PsiUtil.getNearestAffectingScope(this);  // 2, 3, 4, 5, 1 for method declarations
        containingScope = PsiUtil.createSmartPointer(scope);
        if ((scope instanceof PascalModuleImpl) && (this instanceof PasRoutineImplDecl)) {            // 1 for method implementations
            String[] names = PsiUtil.getQualifiedMethodName(this).split("\\.");
            if (names.length <= 1) {                                                                            // should not be true
                containingScope = SmartPointerManager.getInstance(scope.getProject()).createSmartPsiElementPointer(scope);
                LOG.info(String.format("ERROR: qualified method name of %s is: %s", getName(), PsiUtil.getQualifiedMethodName(this)));
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
