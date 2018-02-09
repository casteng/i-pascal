package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;

public interface PasClassDeclStub extends StubElement<PascalClassDecl> {
    String getName();

}
