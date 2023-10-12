package com.siberika.idea.pascal.ide.intention;

import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import org.jetbrains.annotations.NotNull;

class CreatePropertyForParamAction extends CreateFieldForParamAction {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.create.property", namedElement != null ? namedElement.getName() : "");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        if ((element instanceof PasNamedIdent) && (element.getParent() instanceof PasFormalParameter)) {
            namedElement = (PascalNamedElement) element;
        }
        PascalRoutine routine = getRoutine(namedElement);
        if (routine != null) {
            PasEntityScope classScope = routine.getContainingScope();
            if (classScope instanceof PascalStructType) {
                String fieldName = getFieldName(namedElement.getName());
                PascalActionDeclare.ActionCreatePropertyHP cfa = new PascalActionDeclare.ActionCreatePropertyHP(getText(), namedElement, RoutineUtil.getParameterType(namedElement), classScope) {
                    public void afterExecution(Editor editor, PsiFile file, TemplateState state) {
                        addParamAssignment(classScope, routine, namedElement, fieldName);
                    }
                };
                cfa.invoke(project, editor, routine.getContainingFile());
            }
        }
    }

}
