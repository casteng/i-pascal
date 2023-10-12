package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PascalInterfaceDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasInterfaceDeclStub;

public abstract class PascalInterfaceDeclImpl extends PasStubStructTypeImpl<PascalInterfaceDecl, PasInterfaceDeclStub> implements PascalInterfaceDecl {

    public PascalInterfaceDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalInterfaceDeclImpl(PasInterfaceDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

}
