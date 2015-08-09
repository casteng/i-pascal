package com.siberika.idea.pascal.editor;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasInterfaceDecl;
import com.siberika.idea.pascal.lang.psi.PasProcBodyBlock;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 06/10/2013
 */
public class PascalRoutineActions {

    public static final FixActionData IMPLEMENT = new FixActionData() {
        @Override
        void calcData(PsiElement section, PascalNamedElement element) {
            PascalRoutineImpl routine = (PascalRoutineImpl) element;
            String prefix = SectionToggle.getPrefix(routine);
            text = element.getText();
            String name = PsiUtil.getFieldName(element);
            text = "\n\n" + StringUtil.replace(text, name, prefix + name) + "\nbegin\n\nend;\n\n";
            offset = SectionToggle.findImplPos(routine);
            parent = routine;
            if (offset < 0) {
                parent = PsiUtil.getModuleImplementationSection(element.getContainingFile());
                parent = parent != null ? PsiTreeUtil.findChildOfType(parent, PasImplDeclSection.class) : null;
                if (null != parent) {
                    offset = parent.getTextRange().getEndOffset();
                } else {                                                // program or library
                    offset = getModuleMainDeclSection(routine.getContainingFile());
                    if (offset >= 0) {
                        parent = routine.getContainingFile();
                    }
                }
            }
            if (offset < 0) {
                parent = null;
            }
        }

        @Override
        String getActionName() {
            return PascalBundle.message("action.implement");
        }
    };

    private static int getModuleMainDeclSection(PsiFile section) {
        PasBlockGlobal block = PsiTreeUtil.findChildOfType(section, PasBlockGlobal.class);
        if (block != null) {
            List<PasRoutineImplDecl> impls = block.getRoutineImplDeclList();
            if (!impls.isEmpty()) {
                return impls.get(impls.size() - 1).getTextRange().getEndOffset();
            }
            return block.getBlockBody().getTextOffset();
        }
        return -1;
    }

    public static final FixActionData DECLARE = new FixActionData() {
        @Override
        void calcData(PsiElement section, PascalNamedElement element) {
            PasRoutineImplDeclImpl routine = (PasRoutineImplDeclImpl) element;

            String prefix = routine.getNamespace() + ".";
            PasProcBodyBlock block = routine.getProcBodyBlock();
            int endoffs = block != null ? block.getStartOffsetInParent() : routine.getTextLength();
            text = "\n" + routine.getText().substring(0, endoffs);
            if (prefix.length() > 1) {
                text = text.replaceFirst(prefix, "");
            }

            parent = routine.getContainingScope();
            offset = SectionToggle.findIntfPos(routine);
            if (offset < 0) {
                if (parent instanceof PascalStructType) {
                    PsiElement pos = PsiUtil.findEndSibling(parent.getFirstChild());
                    offset = pos != null ? pos.getTextRange().getStartOffset() : -1;
                } else {
                    PsiElement pos = PsiUtil.getModuleInterfaceSection(routine.getContainingFile());
                    pos = pos != null ? PsiTreeUtil.findChildOfType(pos, PasInterfaceDecl.class) : null;
                    if (null != pos) {
                        offset = pos.getTextRange().getEndOffset();
                    } else {                                            // program or library. Should not go here.
                        offset = getModuleMainDeclSection(routine.getContainingFile());
                    }
                }
            }
            if (offset < 0) {
                parent = null;
            }
        }

        @Override
        String getActionName() {
            return PascalBundle.message("action.declare");
        }
    };
}
