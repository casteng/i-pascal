package com.siberika.idea.pascal.editor.refactoring;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalRefactoringSupport extends RefactoringSupportProvider {
    @Override
    public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
        PsiElement parent = element.getParent();
        if (parent instanceof PasFormalParameter) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement element, @Nullable PsiElement context) {
        PsiElement parent = element.getParent();
        if (parent instanceof PasFormalParameter) {
            return false;
        }
        return true;
    }
}
