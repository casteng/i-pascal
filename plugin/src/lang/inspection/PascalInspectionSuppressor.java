package com.siberika.idea.pascal.lang.inspection;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalInspectionSuppressor implements InspectionSuppressor {
    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (!(element instanceof PascalNamedElement)) {
            return false;
        }
        PsiElement prev = PsiTreeUtil.prevVisibleLeaf(element);
        return (prev instanceof PsiComment) && "{!}".equals(prev.getText());
    }

    @NotNull
    @Override
    public SuppressQuickFix[] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];
    }
}
