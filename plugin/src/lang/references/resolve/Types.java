package com.siberika.idea.pascal.lang.references.resolve;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static PasField.ValueType retrieveFieldType(@NotNull PasField field, int recursionCount) {
        if (SyncUtil.tryLockQuiet(field.getTypeLock(), SyncUtil.LOCK_TIMEOUT_MS)) {
            try {
                if (!field.isTypeResolved()) {
                    PascalNamedElement el = field.getElement();
                    if (ResolveUtil.isStubPowered(el)) {
                        ResolveContext context = new ResolveContext(field.owner, PasField.TYPES_TYPE, true, null, ModuleUtil.retrieveUnitNamespaces(el));
                        field.setValueType(ResolveUtil.resolveTypeWithStub((PascalStubElement) el, context, recursionCount));
                    } else {
                        field.setValueType(PasReferenceUtil.resolveFieldType(field, true, recursionCount));
                    }
                }
                return field.getValueType();
            } finally {
                field.getTypeLock().unlock();
            }
        } else {
            return null;
        }
    }

    public static String getTypeDefaultValueStr(PasField.ValueType type) {
        if (null == type) {
            return null;
        }
        switch (type.kind) {
            case BOOLEAN:
                return "false";
            case POINTER:
            case CLASSREF:
            case PROCEDURE:
            case ARRAY:
            case STRUCT:
            case VARIANT:
                return "nil";
            case INTEGER:
            case FLOAT:
                return "0";
            case SUBRANGE: {
                PsiElement decl = type.declaration.getElement();
                if (decl instanceof PascalIdentDecl) {
                    final String typeString = ((PascalIdentDecl) decl).getTypeString();
                    if (typeString != null && typeString.toUpperCase().contains("TRUE")) {
                        return "false";
                    }
                }
                return "0";
            }
            case CHAR:
                return "#0";
            case STRING:
                return "''";
            case SET:
                return "[]";
        }
        return "";
    }
}
