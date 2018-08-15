package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public interface PasExportedRoutineStub extends PasNamedStub<PascalExportedRoutine> {
    String getName();

    PasField.Visibility getVisibility();

    boolean isConstructor();

    boolean isFunction();

    String getFunctionTypeStr();

    List<String> getFormalParameterNames();

    List<String> getFormalParameterTypes();

    // How a passed actual parameter will be accessed
    List<ParamModifier> getFormalParameterAccess();
}
