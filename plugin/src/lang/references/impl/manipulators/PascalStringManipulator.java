package com.siberika.idea.pascal.lang.references.impl.manipulators;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.psi.impl.PascalStringImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 21/01/2016
 */
public class PascalStringManipulator extends AbstractElementManipulator<PascalStringImpl> {
    @Override
    public PascalStringImpl handleContentChange(@NotNull PascalStringImpl psi, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        final String oldText = psi.getText();
        final String newText = oldText.substring(0, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset());
        return psi.updateText(newText);
    }

    @NotNull
    @Override
    public TextRange getRangeInElement(@NotNull final PascalStringImpl element) {
        return getStringTokenRange(element);
    }

    private static TextRange getStringTokenRange(final PascalStringImpl element) {
        int textLength = element.getTextLength();
        return textLength >= 2 ? TextRange.from(1, textLength -2) : TextRange.EMPTY_RANGE;
    }
}
