package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
public abstract class PasScopeImpl extends PascalNamedElementImpl implements PasEntityScope {

    protected long buildStamp = 0;
    protected List<PasEntityScope> parentScopes;
    protected PasEntityScope nearestAffectingScope;

    public PasScopeImpl(ASTNode node) {
        super(node);
    }

    public boolean isInterfaceDeclaration() {  // TODO: include code for structs
        return (getClass() == PasExportedRoutineImpl.class) || (getClass() == PasClassMethodImpl.class);
    }

    protected boolean isCacheActual(Object cache, long stamp) throws PasInvalidScopeException {
        if (!PsiUtil.isElementValid(this)) {
            throw new PasInvalidScopeException(this);
        }
        return (getContainingFile() != null) && (cache != null) && (getContainingFile().getModificationStamp() == stamp);
    }

    @Nullable
    @Override
    synchronized public PasEntityScope getNearestAffectingScope() throws PasInvalidScopeException {
        if (null == nearestAffectingScope) {
            calcNearestAffectingScope();
        }
        return nearestAffectingScope;
    }

    private void calcNearestAffectingScope() {
        nearestAffectingScope = PsiUtil.getNearestAffectingScope(this);
    }

}
