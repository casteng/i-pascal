package com.siberika.idea.pascal.lang.references.resolve;

import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.util.ModuleUtil;

import java.util.List;

public class ResolveUnit {
    static void checkForDottedUnitName(NamespaceRec fqn, ResolveContext context, ResolveProcessor processor, List<PasEntityScope> sorted) {
        if (null == context.unitNamespaces) {
            context.unitNamespaces = ModuleUtil.retrieveUnitNamespaces(fqn.getParentIdent());
        }
        PasEntityScope res;
        res = tryUnit(fqn, sorted, "");
        if (res != null) {
            PasField field = res.getField(res.getName());
            processor.process(null, null, field, PasField.FieldType.UNIT);
        } else if (fqn.isTarget()) {            // don't check with prefixes if fqn has more than one level
            NamespaceRec oldFqn = new NamespaceRec(fqn);
            for (String prefix : context.unitNamespaces) {
                fqn.addPrefix(oldFqn, prefix);
                res = tryUnit(fqn, sorted, prefix);
                if (res != null) {
                    PasField field = res.getField(res.getName());
                    processor.process(null, null, field, PasField.FieldType.UNIT);
                }
            }
        }
    }

    private static PasEntityScope tryUnit(NamespaceRec fqn, List<PasEntityScope> sortedUnits, String prefix) {
        for (PasEntityScope namespace : sortedUnits) {
            if (fqn.advance(namespace.getName())) {
                return namespace;
            }
        }
        return null;
    }
}
