package com.siberika.idea.pascal.util;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;

/**
 * Author: George Bakhtadze
 * Date: 03/12/2015
 */
public class TestUtil {
    public static PasEntityScope findClass(PasModule module, String name) {
        PasField parentField = module.getField(name);
        return PasReferenceUtil.retrieveFieldTypeScope(parentField, new ResolveContext(module, PasField.TYPES_TYPE, true, null));
    }
}
