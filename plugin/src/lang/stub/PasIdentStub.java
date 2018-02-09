package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;

public interface PasIdentStub extends StubElement<PascalIdentDecl> {
    String getName();

}
