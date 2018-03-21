package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;

import java.util.List;

public class PasClassDeclStubImpl extends PasStructStubImpl<PascalClassDecl> implements PasClassDeclStub {
    public PasClassDeclStubImpl(StubElement parent, String name, List<String> parentNames, PasClassDeclStubElementType stubElementType) {
        super(parent, name, parentNames, stubElementType);
    }
}
