package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasExportedRoutineStubImpl extends StubBase<PascalExportedRoutine> implements PasExportedRoutineStub {
    private String name;
    private String uniqueName;
    private String canonicalName;
    private PasField.Visibility visibility;
    private boolean constructor;
    private boolean function;
    private String functionTypeStr;

    public PasExportedRoutineStubImpl(StubElement parent, String name, String canonicalName, PasField.Visibility visibility,
                                      boolean constructor, boolean function, String functionTypeStr) {
        super(parent, PasExportedRoutineStubElementType.INSTANCE);
        this.name = name;
        this.uniqueName = (parent instanceof PasNamedStub ? ((PasNamedStub) parent).getUniqueName() + "." : "") + canonicalName;
        this.canonicalName = canonicalName;
        this.visibility = visibility;
        this.constructor = constructor;
        this.function = function;
        this.functionTypeStr = functionTypeStr;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.ROUTINE;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public String getCanonicalName() {
        return canonicalName;
    }

    @Override
    public PasField.Visibility getVisibility() {
        return visibility;
    }

    @Override
    public boolean isConstructor() {
        return constructor;
    }

    @Override
    public boolean isFunction() {
        return function;
    }

    @Override
    public String getFunctionTypeStr() {
        return functionTypeStr;
    }
}
