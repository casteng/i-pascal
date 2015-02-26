package com.siberika.idea.pascal.lang.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.lang.PascalReference;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
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
                        if ((element instanceof PascalNamedElement) &&
                             ((element instanceof PasSubIdent) || (element instanceof PasRefNamedIdent)
                           || (element.getParent() instanceof PascalRoutineImpl))) {
                            String text = ((PascalNamedElement) element).getName();
                            return new PsiReference[]{
                                    new PascalReference(element, new TextRange(0, text.length()))
                            };
                        } else if ((element instanceof PsiCommentImpl) && (((PsiCommentImpl) element).getElementType() == PasTypes.CT_DEFINE)) {
                            return new PsiReference[]{
                                    new PascalCommentReference(element, null)
                            };
                        }
                        return new PsiReference[0];
                    }
                });
    }
}
