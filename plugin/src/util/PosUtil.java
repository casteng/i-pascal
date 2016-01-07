package com.siberika.idea.pascal.util;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasVisibility;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasDeclSection;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: George Bakhtadze
 * Date: 22/12/2015
 */
public class PosUtil {
    /* Returns position for a field in a structure at the beginning of a section of the given visibility or higher.
       If no such section found, returns position before first method for variables or after last method for properties and methods.
       If no methods, returns position before END.
       First is position offset, second is true if needed section exists or false if it should be created.
    */
    public static Pair<Integer, Boolean> findPosInStruct(PascalStructType struct, PasField.FieldType type, int targetVisibility) {
        Map<Integer, PasField.Visibility> sections = new TreeMap<Integer, PasField.Visibility>();
        for (PasVisibility visibility : struct.getVisibilityList()) {
            PasField.Visibility vis = PasField.VISIBILITY_MAP.get(visibility.getText().toUpperCase());
            if (vis != null) {
                sections.put(visibility.getTextRange().getEndOffset(), vis);
            }
        }

        Pair<Integer, Boolean> res = Pair.create(-1, false);
        if (PasField.FieldType.TYPE == type) {
            res = getStructSectionPos(struct.getTypeSectionList());
        } else if (PasField.FieldType.CONSTANT == type) {
            res = getStructSectionPos(struct.getConstSectionList());
        } else {
            for (int i = targetVisibility; i < PasField.VISIBILITY_STR.size(); i++) {
                for (PasVisibility visibility : struct.getVisibilityList()) {
                    if (PasField.VISIBILITY_STR.get(i).equalsIgnoreCase(visibility.getText())) {
                        return Pair.create(visibility.getTextRange().getEndOffset(), res.second);
                    }
                }
            }
        }
        if (!struct.getExportedRoutineList().isEmpty()) {
            if ((PasField.FieldType.VARIABLE == type) || (PasField.FieldType.TYPE == type) || (PasField.FieldType.CONSTANT == type)) {
                return Pair.create(struct.getExportedRoutineList().get(0).getTextRange().getStartOffset(), res.second);
            } else {
                return Pair.create(struct.getExportedRoutineList().get(struct.getExportedRoutineList().size() - 1).getTextRange().getEndOffset(), res.second);
            }
        }
        ASTNode before = null;
        if (struct instanceof PasRecordDecl) {
            before = struct.getNode().findChildByType(PasTypes.CASE);
        }
        before = before != null ? before : struct.getNode().findChildByType(PasTypes.END);
        //PsiElement end = PsiUtil.findEndSibling(struct.getFirstChild());
        return Pair.create(before != null ? before.getTextRange().getStartOffset() : -1, res.second);
    }

    private static <T extends PasDeclSection> Pair<Integer, Boolean> getStructSectionPos(List<T> sectionList) {
        return sectionList.isEmpty() ? Pair.create(-1, false) : Pair.create(sectionList.iterator().next().getTextRange().getEndOffset(), true);
    }
}
