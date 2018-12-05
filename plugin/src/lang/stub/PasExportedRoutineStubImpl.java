package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasExportedRoutineStubImpl extends PasNamedStubBase<PascalExportedRoutine> implements PasExportedRoutineStub {
    private PasField.Visibility visibility;
    private final boolean exported;
    private boolean constructor;
    private boolean function;
    private String functionTypeStr;
    private List<String> parameterNames;
    private List<String> parameterTypes;
    private List<ParamModifier> parameterAccess;

    public PasExportedRoutineStubImpl(StubElement parent, String name, PasField.Visibility visibility, boolean exported,
                                      String containingUnitName, boolean constructor, boolean function, String functionTypeStr,
                                      List<String> parameterNames, List<String> parameterTypes, List<ParamModifier> parameterAccess) {
        super(parent, PasExportedRoutineStubElementType.INSTANCE, name, containingUnitName);
        this.visibility = visibility;
        this.exported = exported;
        this.constructor = constructor;
        this.function = function;
        this.functionTypeStr = functionTypeStr;
        this.parameterNames = parameterNames;
        this.parameterTypes = parameterTypes;
        this.parameterAccess = parameterAccess;
        this.uniqueName = (parent instanceof PasNamedStub ? ((PasNamedStub) parent).getUniqueName() + "." : "") + RoutineUtil.calcCanonicalName(name, parameterTypes, parameterAccess, functionTypeStr);
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.ROUTINE;
    }

    @Override
    public boolean isExported() {
        return exported;
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

    @Override
    public List<String> getFormalParameterTypes() {
        return parameterTypes;
    }

    @Override
    public List<ParamModifier> getFormalParameterAccess() {
        return parameterAccess;
    }
}
