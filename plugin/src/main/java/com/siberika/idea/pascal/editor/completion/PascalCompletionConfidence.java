package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ThreeState;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
public class PascalCompletionConfidence extends CompletionConfidence {
    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        if (PascalLexer.NUMBERS.contains(contextElement.getNode().getElementType())) {
            return ThreeState.YES;
        }
        if ((contextElement.getPrevSibling() != null) && (contextElement.getPrevSibling().getNode() != null)) {

            /*System.out.println("===*** shouldSkipAutopopup: " + contextElement + ", sib: " + contextElement.getPrevSibling()
                    + ", par: " + contextElement.getParent());*/
            IElementType type = contextElement.getPrevSibling().getNode().getElementType();
            if (!isName(type)) {
                type = contextElement.getNode().getElementType();
            }
            if (!isName(type) && !PascalLexer.COMPILER_DIRECTIVES.contains(type) && shouldSkipInComment(contextElement, offset)) {
                return ThreeState.YES;
            }
        }
        return super.shouldSkipAutopopup(contextElement, psiFile, offset);
    }

    private static final Pattern COMMENT_BEGIN = Pattern.compile("\\{\\$?\\w+");

    private boolean shouldSkipInComment(PsiElement contextElement, int offset) {
        int len = offset - contextElement.getTextRange().getStartOffset();
        String text = contextElement.getText().substring(0, len);
        return !COMMENT_BEGIN.matcher(text).matches();
    }

    private boolean isName(IElementType type) {
        return (type == PasTypes.SUB_IDENT) || (type == PasTypes.NAME)
                || (type == PasTypes.CALL_EXPR) || (type == PasTypes.INDEX_EXPR) || (type == PasTypes.DEREFERENCE_EXPR)
                || (type == PasTypes.PAREN_EXPR) || (type == PasTypes.EXPRESSION);
    }
}
