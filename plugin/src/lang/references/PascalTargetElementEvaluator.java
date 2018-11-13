package com.siberika.idea.pascal.lang.references;

import com.intellij.codeInsight.TargetElementEvaluatorEx2;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalTargetElementEvaluator extends TargetElementEvaluatorEx2 {
    @Override
    public boolean includeSelfInGotoImplementation(@NotNull PsiElement element) {
        return true;
    }

    @Nullable
    @Override
    public PsiElement getElementByReference(@NotNull PsiReference ref, int flags) {
        return null;
    }

    @Nullable
    @Override
    public PsiElement getGotoDeclarationTarget(@NotNull PsiElement element, @Nullable PsiElement navElement) {
        if (element instanceof PasExportedRoutine) {
            return SectionToggle.retrieveImplementation((PascalRoutine) element, true);
        }
        return super.getGotoDeclarationTarget(element, navElement);
    }
}
