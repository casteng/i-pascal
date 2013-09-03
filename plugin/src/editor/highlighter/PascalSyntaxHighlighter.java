package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORDS = createTextAttributesKey("Pascal keyword", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey NUMBERS = createTextAttributesKey("Pascal number", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING = createTextAttributesKey("Pascal string", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey COMMENT = createTextAttributesKey("Pascal comment", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey OPERATORS = createTextAttributesKey("Pascal operation", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey SEMICOLON = createTextAttributesKey("Pascal semicolon", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey PARENTHESES = createTextAttributesKey("Pascal parentheses", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey SYMBOLS = createTextAttributesKey("Pascal symbol", DefaultLanguageHighlighterColors.COMMA);

    private final Map<IElementType, TextAttributesKey> colors = new HashMap<IElementType, TextAttributesKey>();

    public PascalSyntaxHighlighter() {
        colors.put(PascalLexer.STRING_LITERAL, STRING);
        colors.put(PascalLexer.COMMENT, COMMENT);
        colors.put(PascalLexer.SEMI, SEMICOLON);

        colors.put(TokenType.BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);

        fillMap(colors, PascalLexer.NUMBERS, NUMBERS);           // TODO: change to safeMap when it will be supported by ultimate edition
        fillMap(colors, PascalLexer.KEYWORDS, KEYWORDS);
        fillMap(colors, PascalLexer.OPERATORS, OPERATORS);
        fillMap(colors, PascalLexer.PARENS, PARENTHESES);
        fillMap(colors, PascalLexer.SYMBOLS, SYMBOLS);
    }

    @NotNull
    public Lexer getHighlightingLexer() {
        return new PascalLexer(null);
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(colors.get(tokenType));
    }

}
