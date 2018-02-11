package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasModuleStubImpl extends StubBase<PascalModule> implements PasModuleStub {

    private String name;
    private PascalModule.ModuleType moduleType;

    public PasModuleStubImpl(StubElement parent, String name, PascalModule.ModuleType moduleType) {
        super(parent, PasModuleStubElementType.INSTANCE);
        this.name = name;
        this.moduleType = moduleType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.UNIT;
    }

    @Override
    public PascalModule.ModuleType getModuleType() {
        return moduleType;
    }
}
