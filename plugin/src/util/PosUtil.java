package com.siberika.idea.pascal.util;

import com.intellij.lang.ASTNode;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasVisibility;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

/**
 * Author: George Bakhtadze
 * Date: 22/12/2015
 */
public class PosUtil {
    /* Returns position for a field in a structure at the beginning of a section of the given visibility or higher.
       If no such section found, returns position before first method for variables or after last method for properties and methods.
       If no methods, returns position before END.
    */
    public static int findPosInStruct(PascalStructType struct, PasField.FieldType type, int targetVisibility) {
        for (int i = targetVisibility; i < PasField.VISIBILITY_STR.size(); i++) {
            for (PasVisibility visibility : struct.getVisibilityList()) {
                if (PasField.VISIBILITY_STR.get(i).equalsIgnoreCase(visibility.getText())) {
                    return visibility.getTextRange().getEndOffset();
                }
            }
        }
        if (!struct.getExportedRoutineList().isEmpty()) {
            if (PasField.FieldType.VARIABLE == type) {
                return struct.getExportedRoutineList().get(0).getTextRange().getStartOffset();
            } else {
                return struct.getExportedRoutineList().get(struct.getExportedRoutineList().size()-1).getTextRange().getEndOffset();
            }
        }
        ASTNode before = null;
        if (struct instanceof PasRecordDecl) {
            before = struct.getNode().findChildByType(PasTypes.CASE);
        };
        before = before != null ? before : struct.getNode().findChildByType(PasTypes.END);
        //PsiElement end = PsiUtil.findEndSibling(struct.getFirstChild());
        return before != null ? before.getTextRange().getStartOffset() : -1;
    }
}
