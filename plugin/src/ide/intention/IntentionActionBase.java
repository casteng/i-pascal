package com.siberika.idea.pascal.ide.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class IntentionActionBase implements IntentionAction {
    private String text = "";

    @Override
    @NotNull
    public String getText() {
        return text;
    }

    protected void setText(@NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String text) {
        this.text = text;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "Pascal";
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        return element != null && isAvailable(file, element);
    }

    protected boolean isAvailable(PsiFile file, PsiElement element) {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        invoke(file, element, editor);
    }

    protected void invoke(PsiFile file, PsiElement element, Editor editor) {
    }

    @Override
    public String toString() {
        return getText();
    }
}
