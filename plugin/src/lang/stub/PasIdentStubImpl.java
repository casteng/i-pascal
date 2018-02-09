package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;

public class PasIdentStubImpl extends StubBase<PascalIdentDecl> implements PasIdentStub {

    private String name;

    public PasIdentStubImpl(StubElement parent, String name) {
        super(parent, PasIdentStubElementType.INSTANCE);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
