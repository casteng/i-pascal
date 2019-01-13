package com.siberika.idea.pascal.editor.formatter;

import com.intellij.lang.Language;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.source.codeStyle.SemanticEditorPosition;
import com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider;
import com.intellij.psi.tree.IElementType;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.ArrayClosingBracket;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.ArrayOpeningBracket;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.BlockClosingBrace;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.BlockComment;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.BlockOpeningBrace;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.Colon;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.Comma;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.DoKeyword;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.ElseKeyword;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.ForKeyword;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.IfKeyword;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.LeftParenthesis;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.RightParenthesis;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.Semicolon;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.TryKeyword;
import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.Whitespace;

public class PascalLineIndentProvider extends JavaLikeLangLineIndentProvider {

    private final static HashMap<IElementType, SemanticEditorPosition.SyntaxElement> SYNTAX_MAP = new HashMap<>();
    static {
        SYNTAX_MAP.put(TokenType.WHITE_SPACE, Whitespace);
        SYNTAX_MAP.put(PasTypes.SEMI, Semicolon);
        SYNTAX_MAP.put(PasTypes.BEGIN, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.REPEAT, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.END, BlockClosingBrace);
        SYNTAX_MAP.put(PasTypes.UNTIL, BlockClosingBrace);
        SYNTAX_MAP.put(PasTypes.LBRACK, ArrayOpeningBracket);
        SYNTAX_MAP.put(PasTypes.RBRACK, ArrayClosingBracket);
        SYNTAX_MAP.put(PasTypes.RPAREN, RightParenthesis);
        SYNTAX_MAP.put(PasTypes.LPAREN, LeftParenthesis);
        SYNTAX_MAP.put(PasTypes.COLON, Colon);
        SYNTAX_MAP.put(PasTypes.IF, IfKeyword);
        SYNTAX_MAP.put(PasTypes.WHILE, IfKeyword);
        SYNTAX_MAP.put(PasTypes.ELSE, ElseKeyword);
        SYNTAX_MAP.put(PasTypes.FOR, ForKeyword);
        SYNTAX_MAP.put(PasTypes.DO, DoKeyword);
        SYNTAX_MAP.put(PasTypes.COMMENT, BlockComment);
        SYNTAX_MAP.put(PasTypes.COMMA, Comma);
        SYNTAX_MAP.put(PasTypes.TRY, TryKeyword);

        SYNTAX_MAP.put(PasTypes.TYPE, DoKeyword);
        SYNTAX_MAP.put(PasTypes.VAR, DoKeyword);
        SYNTAX_MAP.put(PasTypes.CONST, DoKeyword);
    }

    @Nullable
    @Override
    protected SemanticEditorPosition.SyntaxElement mapType(@NotNull IElementType tokenType) {
        SYNTAX_MAP.put(PasTypes.TRY, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.EXCEPT, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.FINALLY, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.OF, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.RECORD, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.CLASS, BlockOpeningBrace);
        SYNTAX_MAP.put(PasTypes.OBJECT, BlockOpeningBrace);
        return SYNTAX_MAP.get(tokenType);
    }
  
    @Override
    public boolean isSuitableForLanguage(@NotNull Language language) {
        return language instanceof PascalLanguage;
    }
}
