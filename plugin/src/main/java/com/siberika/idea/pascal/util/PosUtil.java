package com.siberika.idea.pascal.util;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasClassState;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasVisibility;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 22/12/2015
 */
public class PosUtil {
    /* Returns position for a field in a structure at the end of a section of the given visibility or higher.
       If no such section found, returns position before first method or property for variables or after last method/property for methods/properties.
       If no methods/properties, returns position before END.
       First is position offset, second is true if needed TYPE/CONST section exists or false if it should be created.
    */
    public static Pair<Integer, Boolean> findPosInStruct(PascalStructType struct, PasField.FieldType type, PasField.Visibility targetVisibility) {
        Offsets section = new Offsets(PasField.Visibility.PUBLISHED, getBeginOffset(struct));

        List<PasVisibility> visibilityList = struct.getVisibilityList();
        for (PasVisibility visibility : visibilityList) {
            if (null == section.end) {
                section.end = visibility.getTextOffset();
            }
            PasField.Visibility vis = PasField.Visibility.byKey(visibility.getText().toUpperCase());
            if (!vis.moreStrictThan(targetVisibility) && !section.visibility.moreStrictThan(vis)) {
                section = new Offsets(vis, visibility.getTextRange().getEndOffset());
            }
        }
        if (null == section.end) {
            ASTNode before = null;
            if (struct instanceof PasRecordDecl) {
                before = struct.getNode().findChildByType(PasTypes.CASE);
            }
            before = before != null ? before : struct.getNode().findChildByType(PasTypes.END);
            section.end = before != null ? before.getTextRange().getStartOffset() : struct.getTextRange().getEndOffset();
        }

        if (PasField.FieldType.TYPE == type) {
            Integer offs = getFirstOffsetInSection(section, struct.getTypeSectionList());
            return offs != null ? Pair.create(offs, true) : Pair.create(section.begin, false);
        } else if (PasField.FieldType.CONSTANT == type) {
            Integer offs = getFirstOffsetInSection(section, struct.getConstSectionList());
            return offs != null ? Pair.create(offs, true) : Pair.create(section.begin, false);
        }
        Integer offs = null;
        if ((PasField.FieldType.VARIABLE == type)) {
            List<? extends PsiElement> list = struct.getExportedRoutineList();
            if (list.isEmpty()) {
                list = struct.getClassPropertyList();
            }
            offs = getFirstOffsetInSection(section, list);
        } else if ((PasField.FieldType.PROPERTY == type)) {
            offs = getLastOffsetInSection(section, struct.getClassPropertyList());
        } else if ((PasField.FieldType.ROUTINE == type)) {
            offs = getLastOffsetInSection(section, struct.getExportedRoutineList());
            offs = offs != null ? offs : getFirstOffsetInSection(section, struct.getClassPropertyList());
        }
        //PsiElement end = PsiUtil.findEndSibling(struct.getFirstChild());
        return Pair.create(offs != null ? offs : section.end, true);
    }

    private static int getBeginOffset(PascalStructType struct) {
        if (struct.getClassParent() != null) {
            return struct.getClassParent().getTextRange().getEndOffset();
        } else if (struct instanceof PasClassTypeDecl) {
            PasClassState state = ((PasClassTypeDecl) struct).getClassState();
            if (state != null) {
                return state.getTextRange().getEndOffset();
            }
        }
        return struct.getFirstChild().getTextRange().getEndOffset();
    }

    private static <T extends PsiElement> Integer getFirstOffsetInSection(Offsets section, List<T> elements) {
        for (T element : elements) {
            if (section.contains(element)) {
                return element.getTextRange().getStartOffset();
            }
        }
        return null;
    }

    private static <T extends PsiElement> Integer getLastOffsetInSection(Offsets section, List<T> elements) {
        T last = null;
        for (T element : elements) {
            if (section.contains(element)) {
                last = element;
            }
        }
        return last != null ? last.getTextRange().getEndOffset() : null;
    }

    private static class Offsets {
        private final PasField.Visibility visibility;
        private final int begin;
        private Integer end;

        private Offsets(PasField.Visibility visibility, int begin) {
            this.visibility = visibility;
            this.begin = begin;
        }

        public <T extends PsiElement> boolean contains(T element) {
            return (element.getTextRange().getStartOffset() >= begin) && (element.getTextRange().getEndOffset() < end);
        }
    }
}
