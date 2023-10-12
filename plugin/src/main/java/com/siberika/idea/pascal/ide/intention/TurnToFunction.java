package com.siberika.idea.pascal.ide.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

class TurnToFunction extends RoutineIntention {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.routine.to.function");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return PascalBundle.message("action.fix.routine.to.function.family");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PascalRoutine routine = getRoutineHeader(editor, element);
        return (routine != null) && !routine.isFunction();
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PascalRoutine routine = getRoutineHeader(editor, element);
        if ((null != routine) && !routine.isFunction()) {
            PascalRoutine target = getTargetRoutine(editor, element);
            changeToFunction(project, editor, routine, true);
            if (target != null && !target.isFunction()) {
                changeToFunction(project, editor, target, false);
            }
        }
    }

    private void changeToFunction(Project project, Editor editor, PascalRoutine routine, boolean moveCaret) {
        PasFormalParameterSection params = routine.getFormalParameterSection();
        PsiElement anchor = params != null ? params : PsiTreeUtil.getChildOfType(routine, PascalNamedElement.class);
        if (anchor != null) {
            PsiElement added = routine.addAfter(PasElementFactory.createLeafFromText(project, ":"), anchor);
            if ((added != null) && moveCaret) {
                editor.getCaretModel().moveToOffset(added.getTextOffset() + 1);
            }
        }
        switchKeyword(project, routine, PasTypes.PROCEDURE, "function");
    }

}
