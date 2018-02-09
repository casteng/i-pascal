package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasClassDeclStub;

public class PascalClassDeclImpl extends PasStubStructTypeImpl<PasClassDeclStub> implements PascalClassDecl {

    public PascalClassDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalClassDeclImpl(PasClassDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

}
