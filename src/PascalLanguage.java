package com.siberika.idea.pascal;

import com.intellij.lang.Language;

/**
 * User: George Bakhtadze
 * Date: 09.12.2012
 */
public class PascalLanguage extends Language {

    public static final PascalLanguage INSTANCE = new PascalLanguage();

    protected PascalLanguage() {
        super("Pascal");
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return "Pascal Source";
    }
}
