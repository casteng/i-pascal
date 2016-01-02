package com.siberika.idea.pascal.util;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;

/**
 * Author: George Bakhtadze
 * Date: 02/01/2016
 */
public class PreserveCaretTemplateAdapter extends TemplateEditingAdapter {
    private final Editor editor;
    private final RangeMarker marker;

    public PreserveCaretTemplateAdapter(Editor editor, RangeMarker marker) {
        this.editor = editor;
        this.marker = marker;
    }

    @Override
    public void templateFinished(Template template, boolean brokenOff) {
        System.out.println("templateFinished(), brokenOff: " + brokenOff);
        if (marker.isValid()) {
            editor.getCaretModel().moveToOffset(marker.getStartOffset());
        }
    }
}
