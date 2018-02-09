package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;

public class PasClassDeclStubImpl extends StubBase<PascalClassDecl> implements PasClassDeclStub {

    private String name;

    public PasClassDeclStubImpl(StubElement parent, String name) {
        super(parent, PasClassDeclStubElementType.INSTANCE);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
