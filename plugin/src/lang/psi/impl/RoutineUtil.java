package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.PsiUtil;

class RoutineUtil {
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

    public static boolean isConstructor(PsiElement routine) {
        return routine.getFirstChild().getNode().getElementType() == PasTypes.CONSTRUCTOR;
    }
}
