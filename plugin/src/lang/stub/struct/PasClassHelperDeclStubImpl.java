package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalClassHelperDecl;

import java.util.List;

public class PasClassHelperDeclStubImpl extends PasStructStubImpl<PascalClassHelperDecl> implements PasClassHelperDeclStub {
    public PasClassHelperDeclStubImpl(StubElement parent, String name, List<String> parentNames, List<String> aliases,
                                      PasClassHelperDeclStubElementType stubElementType) {
        super(parent, name, parentNames, aliases, stubElementType);
    }
}
