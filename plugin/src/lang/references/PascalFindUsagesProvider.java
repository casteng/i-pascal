package com.siberika.idea.pascal.lang.references;

import com.intellij.find.impl.HelpID;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Date: 3/14/13
 * Author: George Bakhtadze
 */
public class PascalFindUsagesProvider implements FindUsagesProvider {
    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return null;
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return (psiElement instanceof PsiNamedElement) || (PascalReferenceContributor.COMMENT_REFERENCE_TOKENS.contains(psiElement.getNode().getElementType()));
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return HelpID.FIND_IN_PROJECT;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if (element instanceof PascalNamedElement) {
            return ((PascalNamedElement) element).getType().name().toLowerCase();
        } else {
            return "identifier";
        }
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof PasEntityScope) {
            return ((PasEntityScope) element).getName();
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof PasGenericTypeIdent) {
            return ((PasGenericTypeIdent) element).getName() + " = ";
        } else {
            return "";
        }
    }
}
