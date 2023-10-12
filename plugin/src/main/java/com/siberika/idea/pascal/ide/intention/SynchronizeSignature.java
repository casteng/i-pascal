package com.siberika.idea.pascal.ide.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasConstrainedTypeParam;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class SynchronizeSignature extends RoutineIntention {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.signature.synchronize");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return PascalBundle.message("action.fix.signature.synchronize.family");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PascalRoutine routine = getRoutineHeader(editor, element);
        PascalRoutine target = getTargetRoutine(editor, element);
        return (routine != null) && (target != null) && !getSignature(routine).equals(getSignature(target));
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PascalRoutine routine = getRoutineHeader(editor, element);
        PascalRoutine target = getTargetRoutine(editor, element);
        if ((null == routine) || (null == target)) {
            return;
        }
        syncFormalParameters(routine, target);
        syncRoutineType(project, routine, target);
    }

    private void syncRoutineType(Project project, PascalRoutine routine, PascalRoutine target) {
        if (routine.isProcedure() && target.isFunction()) {
            changeToProcedure(project, target);
        } else if (routine.isFunction()) {
            if (target.isProcedure()) {
                switchKeyword(project, target, PasTypes.PROCEDURE, "function");
            }
            // add return type
            PsiElement anchor = target.getFormalParameterSection();
            anchor = anchor != null ? anchor : getFormalParametersStart(target);
            if (anchor != null) {
                clearReturnType(target);
                copyReturnType(routine, target, anchor);
            }
        }
    }

    private void copyReturnType(PascalRoutine routine, PascalRoutine target, PsiElement anchor) {
        Pair<PsiElement, PsiElement> returnType = findReturnType(routine);
        if (returnType != null) {
            PsiElement el = returnType.first;
            while (el != returnType.second) {
                target.addAfter(el, anchor);
                el = el.getNextSibling();
                anchor = anchor.getNextSibling();
            }
            target.addAfter(el, anchor);
        }
    }

    private void syncFormalParameters(PascalRoutine routine, PascalRoutine target) {
        PasFormalParameterSection params = routine.getFormalParameterSection();
        PasFormalParameterSection targetParams = target.getFormalParameterSection();
        if (null == params) {
            if (targetParams != null) {
                targetParams.delete();
            }
        } else {
            if (targetParams != null) {
                targetParams.replace(params);
            } else {
                PsiElement anchor = getFormalParametersStart(target);
                if (anchor != null) {
                    target.addAfter(params, anchor);
                }
            }
        }
    }

    private PsiElement getFormalParametersStart(PascalRoutine target) {
        PsiElement anchor = null;
        for (PsiElement child : target.getChildren()) {
            if (PsiUtil.isInstanceOfAny(child, PasNamedIdent.class, PasClassQualifiedIdent.class)) {
                anchor = child;
            }
            if (child instanceof PasConstrainedTypeParam) {
                anchor = child.getNextSibling();                       // closing >
            }
        }
        return anchor;
    }

    private String getSignature(@Nullable PascalRoutine routine) {
        PasFormalParameterSection params = routine != null ? routine.getFormalParameterSection() : null;
        String result = params != null ? params.getText() : "";
        Pair<PsiElement, PsiElement> returnType = findReturnType(routine);
        if ((returnType != null) && (routine != null)) {
            return result + routine.getText().substring(returnType.first.getStartOffsetInParent(), returnType.second.getStartOffsetInParent() + returnType.second.getTextLength());
        }
        return result;
    }

}
