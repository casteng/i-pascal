package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalInterfaceDecl;

import java.util.List;

public class PasInterfaceDeclStubImpl extends PasStructStubImpl<PascalInterfaceDecl> implements PasInterfaceDeclStub {
    public PasInterfaceDeclStubImpl(StubElement parent, String name, String containingUnitName, boolean local,
                                    List<String> parentNames, List<String> aliases, PasInterfaceDeclStubElementType stubElementType, List<String> typeParameters) {
        super(parent, name, containingUnitName, local, parentNames, aliases, stubElementType, typeParameters);
    }
}
