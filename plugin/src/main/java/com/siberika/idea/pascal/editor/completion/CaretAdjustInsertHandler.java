package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.siberika.idea.pascal.util.DocUtil;

public class CaretAdjustInsertHandler implements InsertHandler<LookupElement> {

    private final int offset;

    public CaretAdjustInsertHandler(int offset) {
        this.offset = offset;
    }

    @Override
    public void handleInsert(final InsertionContext context, LookupElement item) {
        final PsiElement ending = context.getFile().findElementAt(context.getTailOffset());
        if (!(ending instanceof LeafPsiElement) || !";".equals(ending.getText())) {
            DocUtil.adjustDocument(context.getDocument(), context.getTailOffset(), ";");
        }
        context.getEditor().getCaretModel().moveToOffset(offset);
    }

}
