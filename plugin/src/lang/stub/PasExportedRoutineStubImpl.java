package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasExportedRoutineStubImpl extends PasNamedStubBase<PascalExportedRoutine> implements PasExportedRoutineStub {
    private String canonicalName;
    private PasField.Visibility visibility;
    private boolean constructor;
    private boolean function;
    private String functionTypeStr;
    private List<String> parameterNames;

    public PasExportedRoutineStubImpl(StubElement parent, String name, String canonicalName, PasField.Visibility visibility,
                                      String containingUnitName, boolean constructor, boolean function, String functionTypeStr, List<String> parameterNames) {
        super(parent, PasExportedRoutineStubElementType.INSTANCE, name, containingUnitName);
        this.canonicalName = canonicalName;
        this.visibility = visibility;
        this.constructor = constructor;
        this.function = function;
        this.functionTypeStr = functionTypeStr;
        this.parameterNames = parameterNames;
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.ROUTINE;
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

    @Override
    public List<String> getFormalParameterNames() {
        return parameterNames;
    }
}
