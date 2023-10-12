package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.parameterInfo.ParameterInfoUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasConstExpression;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasIndexExpr;
import com.siberika.idea.pascal.lang.psi.PasLiteralExpr;
import com.siberika.idea.pascal.lang.psi.PasParenExpr;
import com.siberika.idea.pascal.lang.psi.PasProductExpr;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasRelationalExpr;
import com.siberika.idea.pascal.lang.psi.PasSumExpr;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnaryExpr;
import com.siberika.idea.pascal.lang.psi.PasUnaryOp;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalOperation;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutineEntity;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.resolve.Resolve;
import com.siberika.idea.pascal.lang.references.resolve.ResolveProcessor;
import com.siberika.idea.pascal.lang.references.resolve.Types;
import com.siberika.idea.pascal.lang.search.routine.FieldMatcher;
import com.siberika.idea.pascal.lang.search.routine.ParamCountFieldMatcher;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author: George Bakhtadze
 * Date: 03/02/2015
 */
public class PascalExpression extends ASTWrapperPsiElement implements PascalPsiElement {

    private static final int MAX_KIND_DIFF = 2;

    public PascalExpression(ASTNode node) {
        super(node);
    }

    public boolean isRoot() {
        return getParent() instanceof PasExpression;
    }

    @Nullable
    public PascalOperation getOperation() {
        return PsiUtil.findImmChildOfAnyType(this, PascalOperation.class);
    }

    public static List<PasField.ValueType> getTypes(PascalExpression expr) {
        List<PasField.ValueType> res;

        if (expr instanceof PasReferenceExpr) {
            res = getChildType(getFirstChild(expr));
            final PasField field = Types.resolveType(retrieveScope(res), ((PasReferenceExpr) expr).getFullyQualifiedIdent());
            PasField.ValueType fieldType = field != null ? field.getValueType(0) : null;
            if (fieldType != null) {
                res.add(fieldType);
            }
        } else if (expr instanceof PasDereferenceExpr) {
            res = getChildType(getFirstChild(expr));
            res.add(PasField.POINTER);
        } else if (expr instanceof PasIndexExpr) {
            res = getChildType(getFirstChild(expr));
            if (!res.isEmpty()) {                                           // Replace scope if indexing default array property
                PasEntityScope scope = res.iterator().next().getTypeScope();
                PascalNamedElement defProp = scope != null ? Resolve.getDefaultProperty(scope) : null;
                if ((defProp != null) && (defProp.getParent() instanceof PasClassProperty)) {
                    PasTypeID typeId = ((PasClassProperty) defProp.getParent()).getTypeID();
                    if (typeId != null) {
                        PasField field = Types.resolveType(scope, typeId.getFullyQualifiedIdent());
                        PasField.ValueType fieldType = field != null ? field.getValueType(0) : null;
                        if (fieldType != null) {
                            res = new SmartList<>(fieldType);
                        }
                    }
                }
            }
            res.add(PasField.ARRAY);
        } else if (expr instanceof PasProductExpr) {                                      // AS operator case
            res = getChildType(getLastChild(expr));
        } else {
            res = getChildType(getFirstChild(expr));
        }

        return res;
    }

    private static List<PasField.ValueType> getChildType(PsiElement child) {
        if (child instanceof PascalExpression) {
            return PascalExpression.getTypes((PascalExpression) child);
        }
        return new SmartList<>();
    }

    private static PsiElement getFirstChild(PascalExpression expr) {
        PsiElement res = expr.getFirstChild();
        while ((res != null) && (res.getClass() == LeafPsiElement.class)) {
            res = res.getNextSibling();
        }
        return res;
    }

    private static PsiElement getLastChild(PascalExpression expr) {
        PsiElement res = expr.getLastChild();
        while ((res != null) && (res.getClass() == LeafPsiElement.class)) {
            res = res.getPrevSibling();
        }
        return res;
    }

    public static PasEntityScope retrieveScope(List<PasField.ValueType> types) {
        PasField.ValueType newScope = null;
        for (PasField.ValueType type : types) {
            if (type.field != null) {
                newScope = type;
            }
        }

        return newScope != null ? newScope.getTypeScope() : null;
    }

    public static String calcFormalParameterType(PsiElement param) {
        PasCallExpr callExpr = RoutineUtil.retrieveCallExpr(param);
        PasFullyQualifiedIdent ident = callExpr != null ? PsiTreeUtil.findChildOfType(callExpr.getExpr(), PasFullyQualifiedIdent.class) : null;
        if (null == ident) {
            return null;
        }

        AtomicReference<String> result = new AtomicReference<>();

        int paramIndex = ParameterInfoUtils.getCurrentParameterIndex(callExpr.getArgumentList().getNode(), param.getTextRange().getStartOffset(), PasTypes.COMMA);

        ResolveContext context = new ResolveContext(PasField.TYPES_PROPERTY_SPECIFIER, true);
        final FieldMatcher matcher = new ParamCountFieldMatcher(ident.getNamePart(), callExpr.getArgumentList().getExprList().size()) {
            @Override
            protected boolean onMatch(final PasField field, final PascalNamedElement element) {
                return false;
            }
        };
        Resolve.resolveExpr(NamespaceRec.fromElement(ident), context,
                new ResolveProcessor() {
                    @Override
                    public boolean process(final PasEntityScope originalScope, final PasEntityScope scope, final PasField field, final PasField.FieldType type) {
                        if (!matcher.process(Collections.singleton(field))) {
                            final PascalNamedElement el = field.getElement();
                            if (el instanceof PascalRoutineEntity) {
                                final PascalRoutineEntity routine = (PascalRoutineEntity) el;
                                if ((paramIndex >= 0) && (paramIndex < routine.getFormalParameterNames().size())) {
                                    result.set(routine.getFormalParameterTypes().get(paramIndex));
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                });

        return result.get();
    }

    public static String inferType(PasExpr expression) {
        return doInferType(expression, false);
    }

    public static String inferType(PsiElement element) {
        if (element instanceof PasExpr) {
            return inferType((PasExpr) element);
        }
        return "T";
    }

    private static String doInferType(PasExpr expression, boolean minus) {
        if (expression instanceof PasLiteralExpr) {
            PsiElement literal = expression.getFirstChild();
            IElementType type = literal.getNode().getElementType();
            if ((type == PasTypes.NUMBER_HEX) || (type == PasTypes.NUMBER_OCT) || (type == PasTypes.NUMBER_BIN)) {
                return Primitive.INTEGER.name;
            } else if ((type == PasTypes.NUMBER_INT)) {
                return calcIntType(literal.getText(), minus);
            } else if (type == PasTypes.NUMBER_REAL) {
                return calcFloatType(literal.getText());
            } else if ((type == PasTypes.TRUE) || (type == PasTypes.FALSE)) {
                return Primitive.BOOLEAN.name;
            } else if (type == PasTypes.NIL) {
                return Primitive.POINTER.name;
            } else if (type == PasTypes.STRING_FACTOR) {
                return Primitive.STRING.name;
            }
        } else if (expression instanceof PasParenExpr) {
            return !((PasParenExpr) expression).getExprList().isEmpty() ? inferType(((PasParenExpr) expression).getExprList().get(0)) : null;
        } else if (expression instanceof PasUnaryExpr) {
            return infereUnaryExprType((PasUnaryExpr) expression);
        } else if (expression instanceof PasSumExpr) {
            PasSumExpr sumExpr = (PasSumExpr) expression;
            return combineType("-".equals(sumExpr.getAddOp().getText()) ? Operation.SUBTRACT : Operation.SUM, sumExpr.getExprList());
        } else if (expression instanceof PasRelationalExpr) {
            return Primitive.BOOLEAN.name;
        } else if (expression instanceof PasProductExpr) {
            return handleProduct((PasProductExpr) expression);
        } else if (expression instanceof PasCallExpr) {
            return inferCallType((PasCallExpr) expression);
        } else {
            List<PasField.ValueType> types = getTypes((PascalExpression) expression);
            PasField.ValueType lastType = null;
            for (PasField.ValueType type : types) {
                if (type.field != null) {
                    lastType = type;
                } else if ((type.kind == PasField.Kind.ARRAY) || (type.kind == PasField.Kind.POINTER)) {
                    lastType = lastType != null ? lastType.baseType : null;
                }
            }
            if (lastType != null) {
                return getTypeIdentifier(lastType);
            }
        }
        return null;
    }

    private static String calcFloatType(String text) {
        int len = text.lastIndexOf('.');
        if (len > 0) {
            len = text.length() - len - 1;
            if (len < 8) {
                return Primitive.SINGLE.name;
            } else if (len > 15) {
                return Primitive.EXTENDED.name;
            }
        }
        return Primitive.DOUBLE.name;
    }

    private static final int MAX_INT32_LENGTH = "2147483648".length();
    private static final int MAX_INT64_LENGTH = "9223372036854775807".length();
    private static final long MAX_INT64 = 9223372036854775807L;
    private static final long MAX_INT32 = 2147483647L;
    private static final long MIN_INT32 = -2147483648L;

    private static String calcIntType(String text, boolean negative) {
        int len = text.length();
        if (len < MAX_INT32_LENGTH) {
            return Primitive.INTEGER.name;
        } else if (len == MAX_INT32_LENGTH) {
            return (!negative && isLEThan(text, MAX_INT32)) || (negative && isLEThan(text, -MIN_INT32)) ? Primitive.INTEGER.name : Primitive.INT64.name;
        } else if (len < MAX_INT64_LENGTH) {
            return Primitive.INT64.name;
        } else if (len == MAX_INT64_LENGTH) {
            return negative || isLEThan(text, MAX_INT64) ? Primitive.INT64.name : Primitive.QWORD.name;
        } else {
            return Primitive.QWORD.name;
        }
    }

    private static boolean isLEThan(String text, long l) {
        try {
            return Long.parseLong(text) <= l;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String handleProduct(PasProductExpr expr) {
        String op = expr.getMulOp().getText().toUpperCase();
        if ("AS".equals(op)) {
            List<PasExpr> exprs = expr.getExprList();
            if (exprs.size() > 1) {
                return exprs.get(1).getText();
            }
        }
        return combineType("/".equals(op) ? Operation.DIVISION : Operation.PRODUCT, expr.getExprList());
    }

    private static String inferCallType(PasCallExpr expression) {
        PasFullyQualifiedIdent ref = PsiTreeUtil.findChildOfType(expression.getExpr(), PasFullyQualifiedIdent.class);
        if (null == ref) {
            return null;
        }
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<PascalRoutine> suitable = new AtomicReference<>();
        if (Resolve.resolveExpr(NamespaceRec.fromElement(ref), new ResolveContext(PasField.TYPES_ROUTINE, true),
                new ResolveProcessor() {
                    @Override
                    public boolean process(final PasEntityScope originalScope, final PasEntityScope scope, final PasField field, final PasField.FieldType type) {
                        PascalNamedElement el = field.getElement();
                        if (el instanceof PascalRoutine) {
                            PascalRoutine suitableRoutine = (PascalRoutine) el;
                            suitable.set(suitableRoutine);
                            if (RoutineUtil.isSuitable(expression, suitableRoutine)) {
                                if (suitableRoutine.isConstructor()) {
                                    // TODO: handle metaclass constructor calls
                                    if (expression.getExpr() instanceof PasReferenceExpr) {
                                        String typeName = expression.getExpr().getText();
                                        result.set(typeName.substring(0, typeName.length() - suitableRoutine.getName().length() - 1));
                                        return false;
                                    }
                                } else {
                                    result.set(suitableRoutine.getFunctionTypeStr());
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                }
        )) {
            if (suitable.get() != null) {
                return suitable.get().getFunctionTypeStr();
            }
        }
        // Handle as typecast
        if ((result.get() == null) && (expression.getExpr() instanceof PasReferenceExpr) && (expression.getArgumentList().getExprList().size() == 1)) {
            return expression.getExpr().getText();
        }
        return result.get();
    }

    private static String infereUnaryExprType(PasUnaryExpr expression) {
        PasUnaryOp op = expression.getUnaryOp();
        if ("@".equals(op.getText())) {
            return Primitive.POINTER.name;
        } else {
            return doInferType(expression.getExpr(), "-".equals(op.getText()));
        }
    }

    private static String combineType(Operation op, List<PasExpr> exprList) {
        PasExpr arg1 = exprList.size() > 0 ? exprList.get(0) : null;
        PasExpr arg2 = exprList.size() > 1 ? exprList.get(1) : null;
        if (arg2 != null) {
            String type1 = inferType(arg1);
            String type2 = inferType(arg2);
            if (null == type1) {
                return type2;
            }
            if (null == type2) {
                return type1;
            }
            Primitive prim1 = toPrimitive(type1);
            if (null == prim1) {
                return type1;
            }
            Primitive prim2 = toPrimitive(type2);
            if (null == prim2) {
                return type2;
            }
            if (Operation.DIVISION.equals(op)) {
                return divisionType(prim1, prim2, type1, type2);
            }

            int diff = Math.abs(prim1.kind - prim2.kind);
            if (diff > MAX_KIND_DIFF) {
                return type1;
            } else if (0 == diff || 2 == diff) {
                return prim1.ordinal() > prim2.ordinal() ? type1 : type2;
            } else {
                int maxKind = Math.max(prim1.kind, prim2.kind);
                if (maxKind == Primitive.SINGLE.kind) {
                    return prim1.ordinal() > prim2.ordinal() ? type1 : type2;
                }
                // signed and unsigned: widen result
                Primitive signedPrim = prim1;
                Primitive unsignedPrim = prim2;
                if (prim1.kind == Primitive.BYTE.kind) {
                    signedPrim = prim2;
                    unsignedPrim = prim1;
                }
                switch (unsignedPrim.size) {
                    case 1: {
                        unsignedPrim = Primitive.SMALLINT;
                        break;
                    }
                    case 2: {
                        unsignedPrim = Primitive.INTEGER;
                        break;
                    }
                    case 4: {
                        unsignedPrim = Primitive.INT64;
                        break;
                    }
                    default: unsignedPrim = Primitive.INT64;
                }
                return unsignedPrim.size >= signedPrim.size ? unsignedPrim.name : signedPrim.name;
            }
        }
        return arg1 != null ? inferType(arg1) : null;
    }

    private static String divisionType(@NotNull Primitive prim1, @NotNull Primitive prim2, String type1, String type2) {
        int kind = Math.max(prim1.kind, prim2.kind);
        if (kind <= Primitive.INTEGER.kind) {                             // Integer operands
            return Primitive.SINGLE.name;
        } else {
            return prim1.ordinal() > prim2.ordinal() ? type1 : type2;
        }
    }

    private static Primitive toPrimitive(String type) {
        type = type.toUpperCase();
        for (Primitive primitive : Primitive.values()) {
            if (primitive.name().equals(type)) {
                return primitive;
            }
        }
        return null;
    }

    @Nullable
    private static String getTypeIdentifier(PasField.ValueType type) {
        if (type.field != null) {
            PascalNamedElement el = type.field.getElement();
            if ((type.field.fieldType == PasField.FieldType.PSEUDO_VARIABLE) && PasEntityScope.BUILTIN_SELF_UPPER.equals(type.field.name.toUpperCase())) {
                return el instanceof PascalStructType ? el.getName() : null;
            } else if (el instanceof PasGenericTypeIdent) {
                return el.getText();
            } else {
                PasTypeDecl res = PsiUtil.getTypeDeclaration(type.field.getElement());
                if (res != null) {
                    return res.getText();
                } else if (type.field.fieldType == PasField.FieldType.CONSTANT) {
                    if ((el != null) && (el.getParent() instanceof PasConstDeclaration)) {
                        PasConstExpression expr = ((PasConstDeclaration) el.getParent()).getConstExpression();
                        return (expr != null) && (expr.getExpression() != null) ? inferType(expr.getExpression().getExpr()) : null;
                    }
                } else if (type.field.fieldType == PasField.FieldType.PROPERTY) {
                    final PascalNamedElement fieldElement = type.field.getElement();
                    final PsiElement parent = fieldElement != null ? fieldElement.getParent() : null;
                    if (parent instanceof PasClassProperty) {
                        PasTypeID typeId = PasReferenceUtil.resolvePropertyType(type.field.owner, type.field.name, (PasClassProperty) parent, 0);
                        return typeId != null ? typeId.getText() : null;
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static String calcAssignStatementType(PsiElement statement) {
        PasAssignPart rightPart = PsiUtil.findImmChildOfAnyType(statement, PasAssignPart.class);
        return (rightPart != null) && (rightPart.getExpression() != null) ? inferType(rightPart.getExpression().getExpr()) : null;
    }

    @SuppressWarnings("unchecked")
    public static String calcAssignExpectedType(PsiElement statement) {
        PasExpression leftPart = PsiUtil.findImmChildOfAnyType(statement, PasAssignPart.class) != null ?
                PsiUtil.findImmChildOfAnyType(statement, PasExpression.class) : null;
        return leftPart != null ? inferType(leftPart.getExpr()) : null;
    }

    public enum Operation {
        SUM, SUBTRACT, PRODUCT, DIVISION
    }

    public enum Primitive {
        BYTE("Byte", 0, 1), WORD("Word", 0, 2), DWORD("DWord", 0, 4), LONGWORD("Longword", 0, 4), CARDINAL("Cardinal", 0, 4), QWORD("QWord", 0, 8),
        SHORTINT("Shortint", 1, 1), SMALLINT("Smallint", 1, 2), INTEGER("Integer", 1, 4), LONGINT("Longint", 1, 4), NATIVEINT("Nativeint", 1, 4), INT64("Int64", 1, 8),
        SINGLE("Single", 2, 4), REAL("Real", 2, 6), DOUBLE("Double", 2, 8), EXTENDED("Extended", 2, 10),
        POINTER("Pointer", 10, 4),
        BYTEBOOL("ByteBool", 20, 1), BOOLEAN("Boolean", 20, 1), WORDBOOL("WordBool", 20, 2), LONGBOOL("LongBool", 20, 4),
        CHAR("Char", 30, 1), STRING("String", 31, 4);

        private final String name;
        private final int kind;
        private final int size;

        Primitive(String name, int kind, int size) {
            this.name = name;
            this.kind = kind;
            this.size = size;
        }
    }

    public PasExpr getExpr() {
        return PsiTreeUtil.getChildOfType(this, PasExpr.class);
    }
}
