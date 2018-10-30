package com.siberika.idea.pascal.lang.context;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasClassPropertySpecifier;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasIndexExpr;
import com.siberika.idea.pascal.lang.psi.PasParenExpr;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasUnaryExpr;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalVariableDeclaration;
import com.siberika.idea.pascal.util.PsiUtil;

public class ContextUtil {
    public static boolean isFieldDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasClassField);
    }

    public static boolean isPropertyDecl(PascalNamedElement entityDecl) {
        return (entityDecl instanceof PasClassProperty) || (entityDecl.getParent() instanceof PasClassProperty);
    }

    /**
     * Checks if the entityDecl is a declaration of variable or formal parameter
     *
     * @param entityDecl entity declaration to check
     * @return true if the entityDecl is a declaration of variable or formal parameter
     */
    public static boolean isVariableDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PascalVariableDeclaration);
    }

    // Checks if the entityDecl is a declaration of constant
    public static boolean isConstDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasConstDeclaration);
    }

    // Checks if the entityDecl is a declaration of an enumeration constant
    public static boolean isEnumDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasEnumType);
    }

    public static boolean belongsToInterface(PsiElement ident) {
        return PsiTreeUtil.getParentOfType(ident, PasUnitInterface.class) != null;
    }

    // Check if the named element is the left part of an assignment statement
    public static boolean isAssignLeftPart(PascalNamedElement element) {
        PsiElement expr = PsiUtil.skipToExpression(element);
        if (expr instanceof PasReferenceExpr) {
            PsiElement parent = expr.getParent();
            parent = parent instanceof PasExpression ? parent : PsiTreeUtil.skipParentsOfType(expr, PasUnaryExpr.class, PasParenExpr.class, PasDereferenceExpr.class, PasIndexExpr.class);
            if (parent instanceof PasExpression) {
                return PsiTreeUtil.skipSiblingsForward(parent, PsiUtil.ELEMENT_WS_COMMENTS) instanceof PasAssignPart;
            }
        }
        return false;
    }

    public static boolean isPropertyGetter(PasClassPropertySpecifier spec) {
        return "read".equalsIgnoreCase(spec.getFirstChild().getText());
    }

}
