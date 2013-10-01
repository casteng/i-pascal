package com.siberika.idea.pascal.util;

import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

/**
 * Author: George Bakhtadze
 * Date: 30/09/2013
 */
public interface FieldCollector {
    boolean fieldExists(PascalNamedElement element);
    void addField(String name, PasField field);
}
