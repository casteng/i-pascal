package com.siberika.idea.pascal.ide.intention;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasConstrainedTypeParam;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasProcBodyBlock;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

class SynchronizeSignature extends BaseElementAtCaretIntentionAction {

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
                PsiElement anchor = null;
                for (PsiElement child : target.getChildren()) {
                    if (PsiUtil.isInstanceOfAny(child, PasNamedIdent.class, PasClassQualifiedIdent.class)) {
                        anchor = child;
                    }
                    if (child instanceof PasConstrainedTypeParam) {
                        anchor = child.getNextSibling();                       // closing >
                    }
                }
                if (anchor != null) {
                    target.addAfter(params, anchor);
                }
            }
        }
    }

    private String getSignature(PascalRoutine routine) {
        PasFormalParameterSection params = routine != null ? routine.getFormalParameterSection() : null;
        return params != null ? params.getText() : "";
    }

    private PascalRoutine getTargetRoutine(Editor editor, PsiElement element) {
        PascalRoutine routine = getRoutineHeader(editor, element);
        PsiElement target = SectionToggle.getRoutineTarget(routine);
        return target instanceof PascalRoutine ? (PascalRoutine) target : null;
    }

    private PascalRoutine getRoutineHeader(Editor editor, PsiElement element) {
        PascalRoutine routine = PsiTreeUtil.getParentOfType(element, PascalRoutine.class);
        if (routine instanceof PascalExportedRoutine) {
            return routine;
        } else if (routine instanceof PasRoutineImplDecl) {
            PasProcBodyBlock block = ((PasRoutineImplDecl) routine).getProcBodyBlock();
            Integer blockOffs = block != null ? block.getTextOffset() : null;
            return (null == blockOffs) || (editor.getCaretModel().getOffset() < blockOffs) ? routine : null;
        } else {
            return null;
        }
    }

}
