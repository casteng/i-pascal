package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;

/**
 * Author: George Bakhtadze
 * Date: 06/09/2013
 */
public abstract class PascalRoutineImpl extends PascalNamedElementImpl {
    public PascalRoutineImpl(ASTNode node) {
        super(node);
    }

    public boolean isInterface() {
        return (getClass() == PasExportedRoutineImpl.class) || (getClass() == PasClassMethodImpl.class);
    }

}
