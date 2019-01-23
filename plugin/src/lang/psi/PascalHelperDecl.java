package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.stub.struct.PasHelperDeclStub;

public interface PascalHelperDecl extends PascalStructType<PasHelperDeclStub> {
    String getTarget();
}
