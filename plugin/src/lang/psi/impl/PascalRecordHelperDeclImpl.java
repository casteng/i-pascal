package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PascalRecordHelperDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasRecordHelperDeclStub;
import org.jetbrains.annotations.Nullable;

public abstract class PascalRecordHelperDeclImpl extends PasStubStructTypeImpl<PascalRecordHelperDecl, PasRecordHelperDeclStub> implements PascalRecordHelperDecl {

    public PascalRecordHelperDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalRecordHelperDeclImpl(PasRecordHelperDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Nullable
    @Override
    public PasClassParent getClassParent() {
        return null;
    }

}
