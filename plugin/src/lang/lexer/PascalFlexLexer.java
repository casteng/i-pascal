package com.siberika.idea.pascal.lang.lexer;

import com.intellij.psi.tree.IElementType;

/**
 * Author: George Bakhtadze
 * Date: 27/08/2013
 */
public interface PascalFlexLexer {
    void define(int pos, CharSequence sequence);
    void unDefine(int pos, CharSequence sequence);

    IElementType handleIf(CharSequence sequence);
    IElementType handleIfDef(CharSequence sequence);
    IElementType handleIfNDef(CharSequence sequence);
    IElementType handleIfOpt(CharSequence sequence);
    IElementType handleElse();
    IElementType handleEndIf();

    IElementType handleInclude(CharSequence sequence);

    IElementType getElement(IElementType elementType);
}
