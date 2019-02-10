package com.siberika.idea.pascal.lang.references.resolve;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

public interface ResolveProcessor {
    // return True to continue processing or False to stop
    boolean process(PasEntityScope originalScope, PasEntityScope scope, PasField field, PasField.FieldType type);
}
