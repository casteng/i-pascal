package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.stub.PasExportedRoutineStub;
import com.siberika.idea.pascal.lang.stub.PasIdentStubImpl;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import com.siberika.idea.pascal.lang.stub.struct.PasStructStub;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
@SuppressWarnings("unchecked")
public abstract class PasStubScopeImpl<B extends PasNamedStub> extends PascalNamedStubElement<B> implements PasEntityScope {

    protected static final Logger LOG = Logger.getInstance(PasStubScopeImpl.class.getName());

    private Map<String, PasNamedStub> fieldsMap = new ConcurrentHashMap<>();

    private volatile String cachedKey;

    PasStubScopeImpl(ASTNode node) {
        super(node);
    }

    PasStubScopeImpl(final B stub, IStubElementType noteType) {
        super(stub, noteType);
    }

    @Override
    protected PascalHelperNamed createHelper() {
        return new PascalHelperScope(this);
    }

    @Override
    public void invalidateCache(boolean subtreeChanged) {
        super.invalidateCache(subtreeChanged);
        if (cachedKey != null) {
            String key = cachedKey;
            PascalModuleImpl.invalidate(key);
            PascalRoutineImpl.invalidate(key);
            PasStubStructTypeImpl.invalidate(key);
            cachedKey = null;
        }
        fieldsMap.clear();                                     // TODO: probably not safe
    }

    @Nullable
    @Override
    public B retrieveStub() {
        return getGreenStub();
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
        if (fieldsMap.containsKey(PascalHelperScope.KEY_EMPTY_MARKER)) {
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
            } else if (stubElement instanceof PasExportedRoutineStub) {
                PasExportedRoutineStub rStub = (PasExportedRoutineStub) stubElement;
                fieldsMap.put(RoutineUtil.calcCanonicalName(rStub.getName(), rStub.getFormalParameterNames(), rStub.getFormalParameterTypes(),
                        rStub.getFormalParameterAccess(), rStub.getFunctionTypeStr()), (PasNamedStub) stubElement);
            }
        }
        fieldsMap.put(PascalHelperScope.KEY_EMPTY_MARKER, new PasIdentStubImpl(null, "", false, "", PasField.FieldType.VARIABLE, "", null, PasField.Access.READONLY, null, null));
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

    <T extends PascalHelperScope.Cached> void ensureChache(Cache<String, T> cache) {
/*        if (!PsiUtil.checkeElement(this)) {
            return false;
        }*/
/*        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return false;
        }*/
        if (!PsiUtil.isElementValid(this)) {
            invalidateCache(false);
            throw new ProcessCanceledException();
        }
        PascalHelperScope.Cached members = cache.getIfPresent(getKey());
        if ((members != null) && (getStamp(getContainingFile()) != members.stamp)) {
            invalidateCache(false);
        }
    }

    static long getStamp(PsiFile file) {
        //return System.currentTimeMillis();
        return file.getModificationStamp();
    }

    @SuppressWarnings("unchecked")
    void collectFields(PsiElement section, PasField.Visibility visibility,
                       final Map<String, PasField> members, final Set<PascalNamedElement> redeclaredMembers) {
        PascalHelperScope.collectFields(this, section, visibility, members, redeclaredMembers);
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
        return getHelper().calcContainingScope();
    }

    private PascalHelperScope getHelper() {
        return (PascalHelperScope) helper;
    }

}
