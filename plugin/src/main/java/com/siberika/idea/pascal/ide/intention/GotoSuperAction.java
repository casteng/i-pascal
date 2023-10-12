package com.siberika.idea.pascal.ide.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.util.EditorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

class GotoSuperAction extends NavIntentionActionBase {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.struct.goto.super");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return GotoSuper.hasSuperTargets(element);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        Collection<PasEntityScope> targets = GotoSuper.search(element).findAll();
        EditorUtil.navigateTo(editor, getText(), targets);
    }
}
