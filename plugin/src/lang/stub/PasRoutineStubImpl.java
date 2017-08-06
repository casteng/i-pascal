package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasRoutineStubImpl extends StubBase<PascalRoutineImpl> implements PasRoutineStub {
    public PasRoutineStubImpl(StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }
}
