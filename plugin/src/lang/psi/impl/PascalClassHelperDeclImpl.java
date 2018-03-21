package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PascalClassHelperDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasClassHelperDeclStub;
import org.jetbrains.annotations.Nullable;

public abstract class PascalClassHelperDeclImpl extends PasStubStructTypeImpl<PascalClassHelperDecl, PasClassHelperDeclStub> implements PascalClassHelperDecl {

    public PascalClassHelperDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalClassHelperDeclImpl(PasClassHelperDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Nullable
    @Override
    public PasClassParent getClassParent() {
        return null;
    }
}
