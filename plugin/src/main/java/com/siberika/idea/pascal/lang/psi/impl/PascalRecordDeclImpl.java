package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PascalRecordDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasRecordDeclStub;

public abstract class PascalRecordDeclImpl extends PasStubStructTypeImpl<PascalRecordDecl, PasRecordDeclStub> implements PascalRecordDecl {

    public PascalRecordDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalRecordDeclImpl(PasRecordDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

}
