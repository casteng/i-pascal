package com.siberika.idea.pascal.util;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.editor.PascalActionDeclare;

/**
 * Author: George Bakhtadze
 * Date: 02/01/2016
 */
public class PreserveCaretTemplateAdapter extends TemplateEditingAdapter {
    private final PsiFile file;
    private final Editor editor;
    private final RangeMarker marker;
    private final PsiElement elementToReformat;
    private final PascalActionDeclare actionDeclare;

    public PreserveCaretTemplateAdapter(Editor editor, PsiFile file, RangeMarker marker, PsiElement elementToReformat, PascalActionDeclare actionDeclare) {
        this.editor = editor;
        this.file = file;
        this.marker = marker;
        this.elementToReformat = elementToReformat;
        this.actionDeclare = actionDeclare;
    }

    @Override
    public void templateFinished(Template template, boolean brokenOff) {
        if ((editor != null) && (editor.getProject() != null) && (file != null) && marker.isValid()) {
            editor.getCaretModel().moveToOffset(marker.getStartOffset());
            editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
            FileEditorManager.getInstance(editor.getProject()).openFile(file.getVirtualFile(), true, true);
            DocUtil.reformat(elementToReformat, true);
            if (actionDeclare != null) {
                actionDeclare.afterExecution(editor, file);
            }
        }
    }
}
