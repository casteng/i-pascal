package com.siberika.idea.pascal.editor;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;

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
            text = "\n\n" + text.replace(name, prefix + name) + "\nbegin\n\nend;";
            parent = SectionToggle.findImplPos(routine);
            if (parent != null) {
                offset = parent.getTextRange().getEndOffset();
            } else {
                parent = PsiUtil.getModuleImplementationSection(element.getContainingFile());
                if (parent != null) {
                    parent = parent.getFirstChild();
                    offset = parent.getTextRange().getEndOffset();
                }
            }
        }

        @Override
        String getActionName() {
            return PascalBundle.message("action.implement");
        }
    };

}
