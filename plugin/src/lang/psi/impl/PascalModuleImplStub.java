package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.stub.PasModuleStub;

import java.util.Collection;

// TODO: merge with PascalModuleImpl
public abstract class PascalModuleImplStub extends PasStubScopeImpl<PasModuleStub> implements PascalModule {
    public PascalModuleImplStub(ASTNode node) {
        super(node);
    }

    public PascalModuleImplStub(PasModuleStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    PasField getFieldStub(String name) {
        PasField result = super.getFieldStub(name);
        if ((null == result) && getName().equalsIgnoreCase(name)) {
            result = new PasField(this.getStub(), null);       // unit name reference
        }
        return result;
    }

    Collection<PasField> getAllFieldsStub() {
        Collection<PasField> res = super.getAllFieldsStub();
        res.add(new PasField(this.getStub(), null));       // unit name reference
        return res;
    }

}
