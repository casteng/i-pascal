package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalClassHelperDecl;

import java.util.List;

public class PasClassHelperDeclStubImpl extends PasStructStubImpl<PascalClassHelperDecl> implements PasClassHelperDeclStub {
    public PasClassHelperDeclStubImpl(StubElement parent, String name, String containingUnitName,
                                      List<String> parentNames, List<String> aliases, PasClassHelperDeclStubElementType stubElementType, List<String> typeParameters) {
        super(parent, name, containingUnitName, parentNames, aliases, stubElementType, typeParameters);
    }
}
