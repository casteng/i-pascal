package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.Nullable;

public interface PascalRoutine extends PascalRoutineEntity, PasEntityScope {

    PasField.Visibility getVisibility();

    String getCanonicalName();

    @Nullable
    PasTypeID getFunctionTypeIdent();                              // TODO: remove

}
