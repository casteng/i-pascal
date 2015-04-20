package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
public abstract class PasScopeImpl extends PascalNamedElementImpl implements PasEntityScope {

    protected boolean building = false;
    protected long buildStamp = -1;
    protected long parentBuildStamp = -1;
    protected List<PasEntityScope> parentScopes;
    protected PasEntityScope nearestAffectingScope;

    public PasScopeImpl(ASTNode node) {
        super(node);
    }

    protected boolean isCacheActual(Object cache, long stamp) throws PasInvalidScopeException {
        if (!PsiUtil.checkeElement(this)) {
            return false;
        }
        return (getContainingFile() != null) && (cache != null) && (PsiUtil.getFileStamp(getContainingFile()) == stamp);
    }

    @Nullable
    @Override
    synchronized public PasEntityScope getNearestAffectingScope() throws PasInvalidScopeException {
        if (null == nearestAffectingScope) {
            calcNearestAffectingScope();
        }
        return nearestAffectingScope;
    }

    private void calcNearestAffectingScope() {
        nearestAffectingScope = PsiUtil.getNearestAffectingScope(this);
    }

    @SuppressWarnings("unchecked")
    protected void collectFields(PsiElement section, PasField.Visibility visibility,
                                 final Map<String, PasField> members, final Set<PascalNamedElement> redeclaredMembers) {
        if (null == section) {
            return;
        }
        for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class)) {
            if (PsiUtil.isSameAffectingScope(PsiUtil.getNearestAffectingDeclarationsRoot(namedElement), section)) {
                if (!PsiUtil.isFormalParameterName(namedElement) && !PsiUtil.isUsedUnitName(namedElement)) {
                    if (PsiUtil.isRoutineName(namedElement)) {
                        namedElement = (PascalNamedElement) namedElement.getParent();
                    }
                    String name = namedElement.getName();
                    String memberName = PsiUtil.getFieldName(namedElement).toUpperCase();
                    PasField existing = members.get(memberName);
                    if (shouldAddField(existing)) {         // Otherwise replace with full declaration
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
        return (null == existing) || (PsiUtil.isForwardClassDecl(existing.element) || (existing.fieldType == PasField.FieldType.ROUTINE));
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

}
