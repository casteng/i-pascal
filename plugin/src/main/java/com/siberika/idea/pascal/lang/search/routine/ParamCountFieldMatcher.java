package com.siberika.idea.pascal.lang.search.routine;

import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

// Matches routines by parameters count, passes types as is (to handle typecasts)
public abstract class ParamCountFieldMatcher implements FieldMatcher {
    private final String name;
    private final int paramsCount;

    private static final List<String> VARARGS_ROUTINES = Arrays.asList("WRITELN", "READLN", "WRITE", "READ", "SETLENGTH", "CONCAT");

    public ParamCountFieldMatcher(final String name, final int paramsCount) {
        this.name = name.toUpperCase();
        this.paramsCount = paramsCount;
    }

    // should returns false to stop processing
    protected abstract boolean onMatch(final PasField field, final PascalNamedElement routine);

    @Override
    public boolean process(final Collection<PasField> fields) {
        final String nameWithParen = name + "(";
        for (PasField field : fields) {
            if (field.fieldType == PasField.FieldType.ROUTINE) {
                if (field.name.equalsIgnoreCase(name) || field.name.toUpperCase().startsWith(nameWithParen)) {
                    PascalNamedElement el = field.getElement();
                    if (el instanceof PascalRoutine) {
                        final PascalRoutine routine = (PascalRoutine) el;
                        int routineParamCount = routine.getFormalParameterNames().size();
                        int routineDefParamCount = routine.getFormalParameterDefaultValues().size();
                        if ((
                                ((routineParamCount - routineDefParamCount <= paramsCount) && (routineParamCount + routineDefParamCount >= paramsCount))
                                || (VARARGS_ROUTINES.contains(name)))
                                && !onMatch(field, routine)) {
                            return false;
                        }
                    }
                }
            } else if (field.name.equalsIgnoreCase(name)) {
                return onMatch(field, field.getElement());
            }
        }
        return true;
    }

}
