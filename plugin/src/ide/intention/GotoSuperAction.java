package com.siberika.idea.pascal.ide.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.util.EditorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

class GotoSuperAction extends IntentionActionBase {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.struct.goto.super");
    }

    @Override
    protected boolean isAvailable(PsiFile file, PsiElement element) {
        return GotoSuper.hasSuperTargets(element);
    }

    @Override
    protected void invoke(PsiFile file, PsiElement element, Editor editor) {
        Collection<PasEntityScope> targets = GotoSuper.search(element).findAll();
        EditorUtil.navigateTo(editor, getText(), targets);
    }

}
