package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalSyntaxHighlighter extends SyntaxHighlighterBase {

    private final TextAttributesKey[] BAD_CHARACTER_KEYS = new TextAttributesKey[]{HighlighterColors.BAD_CHARACTER};

    private final Map<IElementType, TextAttributesKey> colors = new HashMap<IElementType, TextAttributesKey>();

    public PascalSyntaxHighlighter() {
        colors.put(PascalLexer.STRING_LITERAL, LuaHighlightingData.STRING);
        colors.put(PascalLexer.COMMENT, LuaHighlightingData.COMMENT);
        colors.put(PascalLexer.NUMBER_INT, LuaHighlightingData.NUMBER);
        colors.put(PascalLexer.NUMBER_REAL, LuaHighlightingData.NUMBER);
        colors.put(PascalLexer.NUMBER_HEX, LuaHighlightingData.NUMBER);
        colors.put(PascalLexer.NUMBER_BIN, LuaHighlightingData.NUMBER);

        colors.put(TokenType.BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);

        //colors.put(PascalLexer.BLOCK, LuaHighlightingData.KEYWORD);

        fillMap(colors, PascalLexer.KEYWORDS, LuaHighlightingData.KEYWORD);
        fillMap(colors, PascalLexer.OPERATORS, LuaHighlightingData.OPERATORS);
        fillMap(colors, PascalLexer.PARENS, LuaHighlightingData.BRACES);
        fillMap(colors, PascalLexer.SYMBOLS, LuaHighlightingData.COMMA);
    }

    @NotNull
    public Lexer getHighlightingLexer() {
        return new PascalLexer();
    }

    @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return pack(colors.get(tokenType));
  }

}
