package com.siberika.idea.pascal.editor.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.util.Function;

public class PsiElementTrimRenderer<T extends PsiElement> implements Function<T, String> {
    private final int maxLength;

    public PsiElementTrimRenderer(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public String fun(T element) {
        final String text = element.getText();
        int firstNewLinePos = text.indexOf('\n');
        String trimmedText = text.substring(0, firstNewLinePos != -1 ? firstNewLinePos : Math.min(maxLength, text.length()));
        if (trimmedText.length() != text.length()) trimmedText += " ...";
        return trimmedText;
    }
}
