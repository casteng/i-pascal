package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.openapi.util.NullUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ThreeState;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
public class PascalCompletionConfidence extends CompletionConfidence {
    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        if (NullUtils.notNull(contextElement, contextElement.getPrevSibling(), contextElement.getPrevSibling().getNode())) {

            /*System.out.println("===*** shouldSkipAutopopup: " + contextElement + ", sib: " + contextElement.getPrevSibling()
                    + ", par: " + contextElement.getParent());*/

            if (contextElement.getPrevSibling().getNode().getElementType() != PasTypes.NAME) {
                return ThreeState.YES;
            }
        }
        return super.shouldSkipAutopopup(contextElement, psiFile, offset);
    }
}
