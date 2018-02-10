package com.siberika.idea.pascal.ide.actions;

import com.intellij.codeInsight.hint.ImplementationTextSelectioner;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 25/02/2017
 */
public class PascalImplementationTextSelectioner implements ImplementationTextSelectioner {
    @Override
    public int getTextStartOffset(@NotNull PsiElement element) {
        element = findElement(element);
        final TextRange textRange = element.getTextRange();
        return textRange.getStartOffset();
    }

    @Override
    public int getTextEndOffset(@NotNull PsiElement element) {
        element = findElement(element);
        final TextRange textRange = element.getTextRange();
        return textRange.getEndOffset();
    }

    private PsiElement findElement(PsiElement element) {
        if (element instanceof PasExportedRoutine) {
            PsiElement impl = SectionToggle.retrieveImplementation((PascalRoutine) element, true);
            return impl != null ? impl : element;
        } else if ((element instanceof PasGenericTypeIdent) && (element.getParent() instanceof PasTypeDeclaration)) {
            return element.getParent();
        } else if (element.getParent() instanceof PasExportedRoutine) {
            PsiElement impl = SectionToggle.retrieveImplementation((PascalRoutine) element.getParent(), true);
            return impl != null ? impl : element;
        }
        return element;
    }
}
