package com.siberika.idea.pascal.lang.lexer;

import com.intellij.psi.tree.IElementType;

import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 27/08/2013
 */
public interface PascalFlexLexer {
    Pattern PATTERN_DEFINE = Pattern.compile("\\{\\$\\w+\\s+(\\w+)\\s*}");
    Pattern PATTERN_CONDITION = Pattern.compile("(?i)\\{\\$(IF|ELSEIF)\\s+([\\w(][\\w()\\s]*)\\s*}?");

    IElementType STRING_LITERAL_UNC = new PascalElementType("STRING_LITERAL_UNC");

    void define(int pos, CharSequence sequence);
    void unDefine(int pos, CharSequence sequence);

    IElementType handleIf(int pos, CharSequence sequence);
    IElementType handleElseIf(int pos, CharSequence sequence);
    IElementType handleIfDef(int pos, CharSequence sequence);
    IElementType handleIfNDef(int pos, CharSequence sequence);
    IElementType handleIfOpt(int pos, CharSequence sequence);
    IElementType handleElse(int pos);
    IElementType handleEndIf(int pos);

    IElementType handleInclude(int pos, CharSequence sequence);

    IElementType getElement(IElementType elementType);
}
