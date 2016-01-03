package com.siberika.idea.pascal.util;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Author: George Bakhtadze
 * Date: 02/01/2016
 */
public class PreserveCaretTemplateAdapter extends TemplateEditingAdapter {
    private final VirtualFile file;
    private final Editor editor;
    private final RangeMarker marker;

    public PreserveCaretTemplateAdapter(Editor editor, VirtualFile file, RangeMarker marker) {
        this.editor = editor;
        this.file = file;
        this.marker = marker;
    }

    @Override
    public void templateFinished(Template template, boolean brokenOff) {
        System.out.println("templateFinished(), brokenOff: " + brokenOff);
        if ((editor != null) && (editor.getProject() != null) && (file != null) && marker.isValid()) {
            editor.getCaretModel().moveToOffset(marker.getStartOffset());
            editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
            FileEditorManager.getInstance(editor.getProject()).openFile(file, true, true);
        }
    }
}
