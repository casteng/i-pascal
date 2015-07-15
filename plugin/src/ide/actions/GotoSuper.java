package com.siberika.idea.pascal.ide.actions;

import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * Author: George Bakhtadze
 * Date: 02/07/2015
 */
public class GotoSuper implements LanguageCodeInsightActionHandler {
    @Override
    public boolean isValidFor(Editor editor, PsiFile file) {
        return false;
    }

    @Override
    public void invoke(Project project, Editor editor, PsiFile file) {
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
