package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

public interface PasNamedStub<T extends PascalNamedElement> extends StubElement<T> {
    String getName();
    PasField.FieldType getType();

    String getUniqueName();

    String getContainingUnitName();

    boolean isExported();
}
