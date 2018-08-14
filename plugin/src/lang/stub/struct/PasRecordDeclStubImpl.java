package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalRecordDecl;

import java.util.List;

public class PasRecordDeclStubImpl extends PasStructStubImpl<PascalRecordDecl> implements PasRecordDeclStub {
    public PasRecordDeclStubImpl(StubElement parent, String name, String containingUnitName, boolean local,
                                 List<String> parentNames, List<String> aliases, PasRecordDeclStubElementType stubElementType) {
        super(parent, name, containingUnitName, local, parentNames, aliases, stubElementType);
    }
}
