package com.siberika.idea.pascal.editor.formatter;

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * Author: George Bakhtadze
 * Date: 17/02/2015
 */
public class PascalSmartEnterProcessor extends SmartEnterProcessor {
    @Override
    public boolean process(Project project, Editor editor, PsiFile psiFile) {
        //FeatureUsageTracker.getInstance().triggerFeatureUsed("codeassists.complete.statement");
        //return invokeProcessor(editor, psiFile, false);
        return false;
    }
}
