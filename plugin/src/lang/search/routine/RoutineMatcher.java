package com.siberika.idea.pascal.lang.search.routine;

import com.siberika.idea.pascal.lang.psi.impl.PasField;

import java.util.Collection;

public interface RoutineMatcher {
    boolean process(Collection<PasField> fields);
}
