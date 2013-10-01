package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
public class PascalCompletionConfidence extends CompletionConfidence {
    @NotNull
    @Override
    public ThreeState shouldFocusLookup(@NotNull CompletionParameters parameters) {
        return ThreeState.YES;
    }

}
