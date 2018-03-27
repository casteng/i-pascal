package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;

// TODO: merge with PasStubStructTypeImpl
public abstract class PascalStubStructTypeImpl<B extends PasNamedStub<? extends PascalStructType>> extends PasStubScopeImpl<B> {
    public PascalStubStructTypeImpl(ASTNode node) {
        super(node);
    }

    public PascalStubStructTypeImpl(B stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

}
