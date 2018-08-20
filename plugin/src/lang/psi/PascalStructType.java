package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.stub.struct.PasStructStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 29/01/2015
 */
public interface PascalStructType<StubT extends PasStructStub> extends PasEntityScope, PascalStubElement<StubT>, HasTypeParameters {
    @Nullable
    PasClassParent getClassParent();
    @NotNull
    List<PasVisibility> getVisibilityList();
    @NotNull
    List<PasClassField> getClassFieldList();
    @NotNull
    List<PasClassProperty> getClassPropertyList();
    @NotNull
    List<PasVarSection> getVarSectionList();
    @NotNull
    List<PasConstSection> getConstSectionList();
    @NotNull
    List<PasTypeSection> getTypeSectionList();
    @NotNull
    List<PasExportedRoutine> getExportedRoutineList();

    @NotNull
    List<String> getParentNames();

}
