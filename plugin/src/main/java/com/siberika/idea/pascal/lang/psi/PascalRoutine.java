package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PascalRoutine extends PascalRoutineEntity, PasEntityScope {

    @NotNull
    List<PasConstrainedTypeParam> getConstrainedTypeParamList();

    PasField.Visibility getVisibility();

    String getCanonicalName();

    String getReducedName();

    @Nullable
    PasTypeID getFunctionTypeIdent();                              // TODO: remove

    @NotNull
    List<PasCustomAttributeDecl> getCustomAttributeDeclList();

    @Nullable
    PasFormalParameterSection getFormalParameterSection();

    boolean isOverloaded();

    boolean isOverridden();

    boolean isAbstract();

    boolean isVirtual();

    boolean isFinal();
}
