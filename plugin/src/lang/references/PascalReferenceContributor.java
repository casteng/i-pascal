package com.siberika.idea.pascal.lang.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.lang.PascalReference;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
public class PascalReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PascalNamedElement.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        PascalNamedElement psiElement = (PascalNamedElement) element;
                        String text = psiElement.getName();

                        if ((psiElement instanceof PasSubIdent) || (psiElement instanceof PasRefNamedIdent)
                                || (psiElement.getParent() instanceof PasRoutineImplDecl)) {
                            return new PsiReference[]{
                                    new PascalReference(element, new TextRange(0, text.length()))
                            };
                        }
                        return new PsiReference[0];
                    }
                });
    }
}
