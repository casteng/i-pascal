package com.siberika.idea.pascal.lang.references;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
public class PascalReferenceContributor extends PsiReferenceContributor {

    public static final TokenSet COMMENT_REFERENCE_TOKENS = TokenSet.create(PasTypes.INCLUDE, PasTypes.CT_DEFINE);

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiElement.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (COMMENT_REFERENCE_TOKENS.contains(element.getNode().getElementType())) {
                            return new PsiReference[]{
                                    new PascalCommentReference(element)
                            };
                        }
                        return new PsiReference[0];
                    }
                });
    }
}
