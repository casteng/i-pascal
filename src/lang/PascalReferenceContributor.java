package com.siberika.idea.pascal.lang;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.lang.psi.PasIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
public class PascalReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PasTypeID.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
                        String text = (String) literalExpression.getValue();
                        if (text != null && text.startsWith("test.")) {
                            return new PsiReference[]{new PascalReference(element, new TextRange(8, text.length() + 1))};
                        }
                        return new PsiReference[0];
                    }
                });
    }
}
