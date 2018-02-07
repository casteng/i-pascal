package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasExportedRoutineStubImpl extends StubBase<PascalExportedRoutine> implements PasExportedRoutineStub {
    public PasExportedRoutineStubImpl(StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }
}
