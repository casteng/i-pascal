package com.siberika.idea.pascal.editor.formatter;

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 17/02/2015
 */
public class PascalSmartEnterProcessor extends SmartEnterProcessor {
    @Override
    public boolean process(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        FeatureUsageTracker.getInstance().triggerFeatureUsed("codeassists.complete.statement");
        PsiElement el = getStatementAtCaret(editor, psiFile);
        el = PsiTreeUtil.getParentOfType(el, PasStatement.class, PasEntityScope.class);
        if (el != null) {
            CodeStyleManager.getInstance(el.getManager()).reformat(el, true);
        }
        return true;
    }
}
