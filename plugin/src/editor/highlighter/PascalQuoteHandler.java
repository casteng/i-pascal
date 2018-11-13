package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.codeInsight.editorActions.JavaLikeQuoteHandler;
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.lexer.PascalFlexLexer;
import com.siberika.idea.pascal.lang.psi.PasLiteralExpr;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasStringFactor;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;

public class PascalQuoteHandler extends SimpleTokenSetQuoteHandler implements JavaLikeQuoteHandler {

    private static final TokenSet STRING_TOKENS = TokenSet.create(PasTypes.STRING_LITERAL);

    PascalQuoteHandler() {
        super(PasTypes.STRING_LITERAL, PascalFlexLexer.STRING_LITERAL_UNC);
    }

    @Override
    public TokenSet getConcatenatableStringTokenTypes() {
        return STRING_TOKENS;
    }

    @Override
    public String getStringConcatenationOperatorRepresentation() {
        return "+";
    }

    @Override
    public TokenSet getStringTokenTypes() {
        return myLiteralTokenSet;
    }

    @Override
    public boolean isAppropriateElementTypeForLiteral(@NotNull IElementType tokenType) {
        return true;
    }

    @Override
    public boolean needParenthesesAroundConcatenation(PsiElement element) {
        return element.getParent() instanceof PasStringFactor && element.getParent().getParent() instanceof PasLiteralExpr
                && element.getParent().getParent().getParent() instanceof PasReferenceExpr;
    }
}
