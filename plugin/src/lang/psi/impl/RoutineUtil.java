package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasParamType;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.List;

public class RoutineUtil {
    static final TokenSet FUNCTION_KEYWORDS = TokenSet.create(PasTypes.FUNCTION, PasTypes.OPERATOR);

    static String calcKey(PascalRoutine routine) {
        StringBuilder sb = new StringBuilder(PsiUtil.getFieldName(routine));
        sb.append(PsiUtil.isForwardProc(routine) ? "-fwd" : "");
        if (routine instanceof PasExportedRoutine) {
            sb.append("^intf");
        } else {
            sb.append("^impl");
        }

        PasEntityScope scope = routine.getContainingScope();
        sb.append(scope != null ? "." + scope.getKey() : "");

//        System.out.println(String.format("%s:%d - %s", PsiUtil.getFieldName(this), this.getTextOffset(), sb.toString()));
        return sb.toString();
    }

    static boolean isConstructor(PsiElement routine) {
        return routine.getFirstChild().getNode().getElementType() == PasTypes.CONSTRUCTOR;
    }

    static void calcFormalParameterNames(PasFormalParameterSection formalParameterSection, List<String> formalParameterNames, List<PasField.Access> formalParameterAccess) {
        if (formalParameterSection != null) {
            for (PasFormalParameter parameter : formalParameterSection.getFormalParameterList()) {
                PasField.Access access = calcAccess(parameter.getParamType());
                for (PascalNamedElement pasNamedIdent : parameter.getNamedIdentDeclList()) {
                    formalParameterNames.add(pasNamedIdent.getName());
                    formalParameterAccess.add(access);
                }
            }
        }
    }

    private static PasField.Access calcAccess(PasParamType paramType) {
        if (paramType != null) {
            String text = paramType.getText().toUpperCase();
            if ("VAR".equals(text)) {
                return PasField.Access.READWRITE;
            } else if ("OUT".equals(text)) {
                return PasField.Access.WRITEONLY;
            }
        }
        return PasField.Access.READONLY;
    }

    public static PasCallExpr retrieveCallExpr(PascalNamedElement element) {
        PsiElement parent = element.getParent();
        if (parent instanceof PasReferenceExpr) {
            if (parent.getParent() instanceof PasArgumentList) {
                parent = parent.getParent().getParent();
                return parent instanceof PasCallExpr ? (PasCallExpr) parent : null;
            }
        }
        return null;
    }

    public static boolean isSuitable(PasCallExpr expression, PascalRoutine routine) {
        List<String> params = routine.getFormalParameterNames();
        // TODO: make type check and handle overload
        if (params.size() == expression.getArgumentList().getExprList().size()) {
            return true;
        }
        return false;
    }
}
