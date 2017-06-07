package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.impl.PascalModule;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasModuleStubImpl extends StubBase<PascalModule> implements PasModuleStub {
    public PasModuleStubImpl(StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }
}
