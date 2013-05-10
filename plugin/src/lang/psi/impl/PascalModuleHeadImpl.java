package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.siberika.idea.pascal.lang.psi.PascalModuleHead;

/**
 * Author: George Bakhtadze
 * Date: 21/04/2013
 */
public class PascalModuleHeadImpl extends PascalNamedElementImpl implements PascalModuleHead {
    public PascalModuleHeadImpl(ASTNode node) {
        super(node);
    }
}
