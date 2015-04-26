package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
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
        if ((contextElement.getPrevSibling() != null) && (contextElement.getPrevSibling().getNode() != null)) {

            /*System.out.println("===*** shouldSkipAutopopup: " + contextElement + ", sib: " + contextElement.getPrevSibling()
                    + ", par: " + contextElement.getParent());*/
            IElementType type = contextElement.getPrevSibling().getNode().getElementType();
            if (!isName(type)) {
                type = contextElement.getNode().getElementType();
            }
            if (!isName(type)) {
                return ThreeState.YES;
            }
        }
        return super.shouldSkipAutopopup(contextElement, psiFile, offset);
    }

    private boolean isName(IElementType type) {
        return (type == PasTypes.SUB_IDENT) || (type == PasTypes.NAME);
    }
}
