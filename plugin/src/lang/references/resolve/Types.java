package com.siberika.idea.pascal.lang.references.resolve;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;

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
}
