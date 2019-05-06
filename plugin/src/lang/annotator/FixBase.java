package com.siberika.idea.pascal.lang.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.PascalBundle;
import org.jetbrains.annotations.NotNull;

abstract class FixBase<T extends PsiElement> extends BaseIntentionAction {

    protected final T element;

    FixBase(T element) {
        this.element = element;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return PascalBundle.message("action.familyName");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

}
