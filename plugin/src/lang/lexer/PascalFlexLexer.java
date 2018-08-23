package com.siberika.idea.pascal.lang.lexer;

import com.intellij.psi.tree.IElementType;

/**
 * Author: George Bakhtadze
 * Date: 27/08/2013
 */
public interface PascalFlexLexer {
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
