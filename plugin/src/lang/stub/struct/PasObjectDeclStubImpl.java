package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalObjectDecl;

import java.util.List;

public class PasObjectDeclStubImpl extends PasStructStubImpl<PascalObjectDecl> implements PasObjectDeclStub {
    public PasObjectDeclStubImpl(StubElement parent, String name, String containingUnitName,
                                 List<String> parentNames, List<String> aliases, PasObjectDeclStubElementType stubElementType, List<String> typeParameters) {
        super(parent, name, containingUnitName, parentNames, aliases, stubElementType, typeParameters);
    }
}
