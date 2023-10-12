package com.siberika.idea.pascal.ide.intention;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class NavIntentionActionBase extends BaseElementAtCaretIntentionAction {

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "Navigation/" + getClass().getSimpleName();
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

}
