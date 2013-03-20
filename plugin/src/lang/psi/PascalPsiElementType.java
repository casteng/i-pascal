package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.siberika.idea.pascal.PascalLanguage;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalPsiElementType extends IElementType {
    public PascalPsiElementType(String debug) {
        super(debug, PascalLanguage.INSTANCE);
    }
}

