package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalRecordDecl;

import java.util.List;

public class PasRecordDeclStubImpl extends PasStructStubImpl<PascalRecordDecl> implements PasRecordDeclStub {
    public PasRecordDeclStubImpl(StubElement parent, String name, String containingUnitName,
                                 List<String> parentNames, List<String> aliases, PasRecordDeclStubElementType stubElementType, List<String> typeParameters) {
        super(parent, name, containingUnitName, parentNames, aliases, stubElementType, typeParameters);
    }
}
