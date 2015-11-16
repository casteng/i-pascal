package com.siberika.idea.pascal.lang.psi.impl;

import com.google.common.cache.Cache;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
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
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
public abstract class PasScopeImpl extends PascalNamedElementImpl implements PasEntityScope {

    protected static final Logger LOG = Logger.getInstance(PasScopeImpl.class.getName());
    protected static final Members EMPTY_MEMBERS = new Members();

    protected boolean building = false;
    protected long parentBuildStamp = -1;
    protected List<SmartPsiElementPointer<PasEntityScope>> parentScopes;
    protected SmartPsiElementPointer<PasEntityScope> containingScope;

    public PasScopeImpl(ASTNode node) {
        super(node);
    }

    public static void invalidateCaches(String key) {
        PascalModuleImpl.invalidate(key);
        PascalRoutineImpl.invalidate(key);
        PasStructTypeImpl.invalidate(key);
    }

    protected static long getStamp(PsiFile file) {
        //return System.currentTimeMillis();
        return file.getModificationStamp();
    }

    public String getKey() {
        return String.format("%s.%s", getContainingFile() != null ? getContainingFile().getName() : "", PsiUtil.getFieldName(this));
    }

    protected void ensureChache(Cache<String, Members> cache) {
/*        if (!PsiUtil.checkeElement(this)) {
            return false;
        }*/
/*        if (null == getContainingFile()) {
            PascalPsiImplUtil.logNullContainingFile(this);
            return false;
        }*/
        Members members = cache.getIfPresent(getKey());
        if ((members != null) && (getStamp(getContainingFile()) != members.stamp)) {
            invalidateCaches(getKey());
        }
    }

    protected boolean isCacheActual(Object cache, long stamp) {
        if (!PsiUtil.checkeElement(this)) {
            return false;
        }
        return (getContainingFile() != null) && (cache != null) && (getStamp(getContainingFile()) == stamp);
//        return (cache != null) && (stamp > getStamp(getContainingFile()) - CACHE_LIVE_MS);
    }

    @Nullable
    @Override
    synchronized public PasEntityScope getContainingScope() {
        if (null == containingScope) {
            calcContainingScope();
        }
        return containingScope != null ? containingScope.getElement() : null;
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
            if (names.length <= 1) {                                                                            // should not be true
                containingScope = SmartPointerManager.getInstance(scope.getProject()).createSmartPsiElementPointer(scope);
                return;
            }
            PasField field = scope.getField(PsiUtil.cleanGenericDef(names[0]));
            updateContainingScope(field);
            for (int i = 1; i < names.length - 1; i++) {
                updateContainingScope(scope.getField(PsiUtil.cleanGenericDef(names[i])));
            }
        }
    }

    private void updateContainingScope(PasField field) {
        if (null == field) {
            return;
        }
        PasEntityScope scope = PasReferenceUtil.retrieveFieldTypeScope(field);
        if (scope != null) {
            containingScope = SmartPointerManager.getInstance(scope.getProject()).createSmartPsiElementPointer(scope);
        }
    }

    @SuppressWarnings("unchecked")
    protected void collectFields(PsiElement section, PasField.Visibility visibility,
                                 final Map<String, PasField> members, final Set<PascalNamedElement> redeclaredMembers) {
        if (null == section) {
            return;
        }
        for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class, PasClassQualifiedIdent.class)) {
            if (PsiUtil.isSameAffectingScope(PsiUtil.getNearestAffectingDeclarationsRoot(namedElement), section)) {
                if (!PsiUtil.isFormalParameterName(namedElement) && !PsiUtil.isUsedUnitName(namedElement)) {
                    if (PsiUtil.isRoutineName(namedElement)) {
                        namedElement = (PascalNamedElement) namedElement.getParent();
                    }
                    String name = namedElement.getName();
                    String memberName = PsiUtil.getFieldName(namedElement).toUpperCase();
                    PasField existing = members.get(memberName);
                    if (shouldAddField(existing)) {                       // Otherwise replace with full declaration
                        PasField field = addField(this, name, namedElement, visibility);
                        if (existing != null) {
                            field.offset = existing.offset;               // replace field but keep offset to resolve fields declared later
                        } else if (field.fieldType == PasField.FieldType.ROUTINE) {
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
    private boolean shouldAddField(PasField existing) {
        return (null == existing) || (PsiUtil.isForwardClassDecl(existing.getElement()) || (existing.fieldType == PasField.FieldType.ROUTINE));
    }

    private PasField addField(PasEntityScope owner, String name, PascalNamedElement namedElement, PasField.Visibility visibility) {
        PasField.FieldType fieldType = getFieldType(namedElement);
        return new PasField(owner, namedElement, name, fieldType, visibility);
    }

    private static PasField.FieldType getFieldType(PascalNamedElement namedElement) {
        PasField.FieldType type = PasField.FieldType.VARIABLE;
        if (PsiUtil.isTypeName(namedElement)) {
            type = PasField.FieldType.TYPE;
        } else if (namedElement instanceof PascalRoutineImpl) {
            type = PasField.FieldType.ROUTINE;
        } else if (PsiUtil.isConstDecl(namedElement) || PsiUtil.isEnumDecl(namedElement)) {
            type = PasField.FieldType.CONSTANT;
        }

        return type;
    }

    static class Members {
        Map<String, PasField> all = new LinkedHashMap<String, PasField>();
        Set<PascalNamedElement> redeclared = new LinkedHashSet<PascalNamedElement>();
        long stamp;
    }

    static class UnitMembers extends Members {
        List<PasEntityScope> units = Collections.emptyList();
    }

}
