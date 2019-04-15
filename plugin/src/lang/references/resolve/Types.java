package com.siberika.idea.pascal.lang.references.resolve;

import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.util.ModuleUtil;

import java.util.List;

public class Types {
    public static List<PasField.ValueType> getTypes(final PascalExpression expr) {
        return PascalExpression.getTypes(expr);
    }

    public static PasEntityScope retrieveFieldTypeScope(final PasField field, final ResolveContext ctx) {
        return PasReferenceUtil.retrieveFieldTypeScope(field, ctx);
    }

    public static PasEntityScope retrieveScope(final List<PasField.ValueType> types) {
        return PascalExpression.retrieveScope(types);
    }

    public static PasField resolveType(PasEntityScope scope, PasFullyQualifiedIdent fullyQualifiedIdent) {
        ResolveContext context = new ResolveContext(scope, PasField.TYPES_ALL, true, null, ModuleUtil.retrieveUnitNamespaces(fullyQualifiedIdent));

        final FQNResolver fqnResolver = new FQNResolver(scope, NamespaceRec.fromElement(fullyQualifiedIdent), context) {
            @Override
            boolean processField(final PasEntityScope scope, final PasField field) {
                if (!field.isConstructor()) {
                    retrieveFieldTypeScope(field, new ResolveContext(field.owner, PasField.TYPES_TYPE, true, null, context.unitNamespaces));
                }
                result = field;
                return false;
            }
        };
        if (!fqnResolver.resolve(true)) {
            return fqnResolver.result;
        } else {
            return null;
        }
    }
}
