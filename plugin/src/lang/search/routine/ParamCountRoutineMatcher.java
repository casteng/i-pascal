package com.siberika.idea.pascal.lang.search.routine;

import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

import java.util.Collection;

public abstract class ParamCountRoutineMatcher implements RoutineMatcher {
    private final String name;
    private final int paramsCount;

    public ParamCountRoutineMatcher(final String name, final int paramsCount) {
        this.name = name.toUpperCase();
        this.paramsCount = paramsCount;
    }

    protected abstract boolean onMatch(final PasField field, final PascalRoutine routine);

    @Override
    public boolean process(final Collection<PasField> fields) {
        final String nameWithParen = name + "(";
        for (PasField field : fields) {
            if ((field.fieldType == PasField.FieldType.ROUTINE)
                    && (field.name.equalsIgnoreCase(name) || field.name.toUpperCase().startsWith(nameWithParen))) {
                PascalNamedElement el = field.getElement();
                if (el instanceof PascalRoutine) {
                    final PascalRoutine routine = (PascalRoutine) el;
                    if ((routine.getFormalParameterNames().size() == paramsCount) && !onMatch(field, routine)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
