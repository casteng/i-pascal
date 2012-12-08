package com.siberika.idea.pascal.lang;

import com.intellij.lang.CodeDocumentationAwareCommenterEx;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.siberika.idea.pascal.lang.lexer.PascalTokenTypes;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
public class PascalCommenter implements CodeDocumentationAwareCommenterEx {

    public String getLineCommentPrefix() {
        return "--";
    }

    public String getBlockCommentPrefix() {
        return "--[[";
    }

    public String getBlockCommentSuffix() {
        return "]]";

    }

    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    public String getCommentedBlockCommentSuffix() {
        return null;
    }

    public IElementType getLineCommentTokenType() {
        return PascalTokenTypes.SHORTCOMMENT;
    }

    public IElementType getBlockCommentTokenType() {
        return PascalTokenTypes.LONGCOMMENT;
    }

    @Nullable
    public IElementType getDocumentationCommentTokenType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDocumentationCommentPrefix() {
        return "---";
    }

    public String getDocumentationCommentLinePrefix() {
        return "--";
    }

    public String getDocumentationCommentSuffix() {
        return null;
    }

    public boolean isDocumentationComment(PsiComment element) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDocumentationCommentText(PsiElement element) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
