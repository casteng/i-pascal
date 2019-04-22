package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.field.Flag;
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
    private String functionTypeStr;
    private List<String> parameterNames;
    private List<String> parameterTypes;
    private List<ParamModifier> parameterAccess;
    private List<String> parameterValues;

    public PasExportedRoutineStubImpl(StubElement parent, String name, PasField.Visibility visibility, int flags,
                                      String containingUnitName, String functionTypeStr,
                                      List<String> parameterNames, List<String> parameterTypes, List<ParamModifier> parameterAccess, List<String> parameterValues) {
        super(parent, PasExportedRoutineStubElementType.INSTANCE, name, containingUnitName);
        this.visibility = visibility;
        setFlags(flags);
        this.functionTypeStr = functionTypeStr;
        this.parameterNames = parameterNames;
        this.parameterTypes = parameterTypes;
        this.parameterAccess = parameterAccess;
        this.parameterValues = parameterValues;
        this.uniqueName = (parent instanceof PasNamedStub ? ((PasNamedStub) parent).getUniqueName() + "." : "")
                + RoutineUtil.calcCanonicalName(name, parameterNames, parameterTypes, parameterAccess, functionTypeStr, parameterValues);
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.ROUTINE;
    }

    @Override
    public boolean isExported() {
        return isFlagSet(Flag.EXPORTED);
    }

    @Override
    public PasField.Visibility getVisibility() {
        return visibility;
    }

    @Override
    public boolean isConstructor() {
        return isFlagSet(Flag.CONSTRUCTOR);
    }

    @Override
    public boolean isFunction() {
        return isFlagSet(Flag.FUNCTION);
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

    @Override
    public List<String> getFormalParameterValues() {
        return parameterValues;
    }
}
