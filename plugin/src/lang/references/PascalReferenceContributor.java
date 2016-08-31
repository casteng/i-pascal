package com.siberika.idea.pascal.lang.references;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
public class PascalReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiElement.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if ((element instanceof PsiCommentImpl) && (((PsiCommentImpl) element).getElementType() == PasTypes.CT_DEFINE)) {
                            return new PsiReference[]{
                                    new PascalCommentReference(element)
                            };
                        }
                        return new PsiReference[0];
                    }
                });
    }
}
