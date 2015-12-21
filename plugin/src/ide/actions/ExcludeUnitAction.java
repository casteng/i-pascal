package com.siberika.idea.pascal.ide.actions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 18/12/2015
 */
public class ExcludeUnitAction extends BaseIntentionAction {
    private final String name;
    private final PasNamespaceIdent usedUnitName;
    public ExcludeUnitAction(String name, PasNamespaceIdent usedUnitName) {
        this.name = name;
        this.usedUnitName = usedUnitName;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Pascal";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @NotNull
    @Override
    public String getText() {
        return name;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
        if (doc != null) {
            doc.insertString(usedUnitName.getTextRange().getStartOffset(), "{!}");
        }
    }
}
