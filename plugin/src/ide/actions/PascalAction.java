package com.siberika.idea.pascal.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2015
 */
public abstract class PascalAction extends AnAction {
    @Nullable
    protected static PsiElement getElement(AnActionEvent e) {
        PsiFile file = getFile(e);
        Editor editor = getEditor(e);
        if ((null == file) || (null == editor)) {
            return null;
        }
        return file.findElementAt(editor.getCaretModel().getOffset());
    }

    protected static Editor getEditor(AnActionEvent e) {
        return e.getData(PlatformDataKeys.EDITOR);
    }

    protected static PsiFile getFile(AnActionEvent e) {
        return e.getData(LangDataKeys.PSI_FILE);
    }
}
