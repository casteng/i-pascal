package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

class PascalHelperScope extends PascalHelperNamed {

    private static final Logger LOG = Logger.getInstance(PascalHelperScope.class);

    static final String KEY_EMPTY_MARKER = "#";
    static final Members EMPTY_MEMBERS = new Members();

    volatile private SmartPsiElementPointer<PasEntityScope> containingScope;

    PascalHelperScope(PasEntityScope self) {
        super(self);
    }

    @Override
    void invalidateCache(boolean subtreeChanged) {
        super.invalidateCache(subtreeChanged);
        containingScope = null;
    }

    PasEntityScope calcContainingScope() {
        ensureCacheActual();
        if (!PsiUtil.isSmartPointerValid(containingScope)) {
            PasEntityScope scope = PsiUtil.getNearestAffectingScope(self);  // 2, 3, 4, 5, 1 for method declarations
            if ((scope instanceof PascalModuleImpl) && (self instanceof PasRoutineImplDecl)) {            // 1 for method implementations
                String[] names = PsiUtil.getQualifiedMethodName(self).split("\\.");
                if (names.length <= 1) {
                    return scope;
                }
                PasField field = scope.getField(PsiUtil.cleanGenericDef(names[0]));
                scope = updateContainingScope(scope, field);
                for (int i = 1; (i < names.length - 1) && (scope != null); i++) {
                    scope = updateContainingScope(scope, scope.getField(PsiUtil.cleanGenericDef(names[i])));
                }
            }
            containingScope = scope != null ? PsiUtil.createSmartPointer(scope) : null;
        }
        return containingScope != null ? containingScope.getElement() : null;
    }

    private PasEntityScope updateContainingScope(PasEntityScope scope, PasField field) {
        if (null == field) {
            return null;
        }
        PasEntityScope tempScope = PasReferenceUtil.retrieveFieldTypeScope(field, new ResolveContext(field.owner, PasField.TYPES_TYPE,
                true, null, ModuleUtil.retrieveUnitNamespaces(field.owner)));
        return tempScope != null ? tempScope : scope;
    }

    @SuppressWarnings("unchecked")
    static void collectFields(PasEntityScope scope, PsiElement section, PasField.Visibility visibility,
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
                        PasField field = addField(scope, name, namedElement, visibility);
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

        boolean isCachable() {
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

}
