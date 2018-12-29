package com.siberika.idea.pascal.lang.stub.struct;

import com.siberika.idea.pascal.lang.psi.PascalStructType;

public interface PascalHelperDeclStub<T extends PascalStructType> extends PasStructStub<T> {
    String getTarget();
}
