package com.siberika.idea.pascal.lang.lexer;

import com.intellij.util.text.CharArrayCharSequence;

import java.io.Reader;

/**
 * Author: George Bakhtadze
 * Date: 05/04/2013
 */
public class PascalFlexLexer extends _PascalLexer {
    public PascalFlexLexer(Reader in) {
        super(in);
    }

    @Override
    public CharSequence getIncludeContent(CharSequence text) {
        return new CharArrayCharSequence("{Some text}".toCharArray());
    }

}
