package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PascalRoutine extends PasEntityScope {

    String getCanonicalName();

    boolean isConstructor();

    PasField.Visibility getVisibility();

    boolean isFunction();

    @NotNull
    String getFunctionTypeStr();

    @Nullable
    PasFormalParameterSection getFormalParameterSection();         // TODO: replace with getFormalParameters()

    @Nullable
    PasTypeID getFunctionTypeIdent();                              // TODO: remove

}
