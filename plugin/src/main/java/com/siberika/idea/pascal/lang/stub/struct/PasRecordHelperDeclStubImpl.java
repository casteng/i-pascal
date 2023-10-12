package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalHelperDecl;

import java.util.List;

public class PasRecordHelperDeclStubImpl extends PasStructStubImpl<PascalHelperDecl> implements PasHelperDeclStub {
    private String target;

    public PasRecordHelperDeclStubImpl(StubElement parent, String name, String containingUnitName, String target,
                                       List<String> parentNames, List<String> aliases, PasRecordHelperDeclStubElementType stubElementType, List<String> typeParameters) {
        super(parent, name, containingUnitName, parentNames, aliases, stubElementType, typeParameters);
        this.target = target;
    }

    @Override
    public String getTarget() {
        return target;
    }
}
