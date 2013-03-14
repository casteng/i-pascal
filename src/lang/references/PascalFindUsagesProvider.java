package com.siberika.idea.pascal.lang.references;

import com.intellij.find.impl.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lexer.FlexAdapter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Date: 3/14/13
 * Author: George Bakhtadze
 */
public class PascalFindUsagesProvider implements FindUsagesProvider {
    private static final DefaultWordsScanner WORDS_SCANNER =
            new DefaultWordsScanner(new FlexAdapter(PascalLexer.getFlexLexer()),
                    TokenSet.create(PasTypes.GENERIC_TYPE_IDENT), TokenSet.create(PasTypes.COMMENT), TokenSet.EMPTY);

    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return WORDS_SCANNER;
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if (element instanceof PasGenericTypeIdent) {
            return "Type";
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof PasGenericTypeIdent) {
            return ((PasGenericTypeIdent) element).getName();
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
