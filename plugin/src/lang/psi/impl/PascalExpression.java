package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
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
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 03/02/2015
 */
public class PascalExpression extends ASTWrapperPsiElement implements PascalPsiElement {

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
            PasField.ValueType fieldType = resolveType(retrieveScope(res), ((PasReferenceExpr) expr).getFullyQualifiedIdent());
            if (fieldType != null) {
                res.add(fieldType);
            }
        } else if (expr instanceof PasDereferenceExpr) {
            res = getChildType(getFirstChild(expr));
            res.add(new PasField.ValueType(null, PasField.Kind.POINTER, null, null));
        } else if (expr instanceof PasIndexExpr) {
            res = getChildType(getFirstChild(expr));
            if (!res.isEmpty()) {                                           // Replace scope if indexing default array property
                PasEntityScope scope = res.iterator().next().getTypeScope();
                PascalNamedElement defProp = scope != null ? PsiUtil.getDefaultProperty(scope) : null;
                if (defProp instanceof PasClassProperty) {
                    PasTypeID typeId = ((PasClassProperty) defProp).getTypeID();
                    if (typeId != null) {
                        PasField.ValueType fieldType = resolveType(scope, typeId.getFullyQualifiedIdent());
                        if (fieldType != null) {
                            res = new SmartList<PasField.ValueType>(fieldType);
                        }
                    }
                }
            }
            res.add(new PasField.ValueType(null, PasField.Kind.ARRAY, null, null));
        } else if (expr instanceof PasProductExpr) {                                      // AS operator case
            res = getChildType(getLastChild(expr));
        } else {
            res = getChildType(getFirstChild(expr));
        }

        return res;
    }

    private static PasField.ValueType resolveType(PasEntityScope scope, PasFullyQualifiedIdent fullyQualifiedIdent) {
        final Collection<PasField> references = PasReferenceUtil.resolve(null, scope, NamespaceRec.fromElement(fullyQualifiedIdent), PasField.TYPES_ALL, true, 0);
        if (!references.isEmpty()) {
            PasField field = references.iterator().next();
            PasReferenceUtil.retrieveFieldTypeScope(field);
            return field.getValueType();
        }
        return null;
    }

    private static List<PasField.ValueType> getChildType(PsiElement child) {
        if (child instanceof PascalExpression) {
            return PascalExpression.getTypes((PascalExpression) child);
        }
        return new SmartList<PasField.ValueType>();
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

    public static String infereType(PasExpr expression) {
        if (expression instanceof PasLiteralExpr) {
            PsiElement literal = expression.getFirstChild();
            IElementType type = literal.getNode().getElementType();
            if ((type == PasTypes.NUMBER_INT) || (type == PasTypes.NUMBER_HEX) || (type == PasTypes.NUMBER_OCT) || (type == PasTypes.NUMBER_BIN)) {
                return Primitive.INTEGER.name;
            } else if (type == PasTypes.NUMBER_REAL) {
                return Primitive.SINGLE.name;
            } else if ((type == PasTypes.TRUE) || (type == PasTypes.FALSE)) {
                return Primitive.BOOLEAN.name;
            } else if (type == PasTypes.NIL) {
                return Primitive.POINTER.name;
            } else if (type == PasTypes.STRING_FACTOR) {
                return Primitive.STRING.name;
            }
        } else if (expression instanceof PasParenExpr) {
            return !((PasParenExpr) expression).getExprList().isEmpty() ? infereType(((PasParenExpr) expression).getExprList().get(0)) : null;
        } else if (expression instanceof PasUnaryExpr) {
            return infereUnaryExprType((PasUnaryExpr) expression);
        } else if (expression instanceof PasSumExpr) {
            return combineType(Operation.SUM, ((PasSumExpr) expression).getExprList());
        } else if (expression instanceof PasRelationalExpr) {
            return Primitive.BOOLEAN.name;
        } else if (expression instanceof PasProductExpr) {
            if ("AS".equalsIgnoreCase(((PasProductExpr) expression).getMulOp().getText())) {
                List<PasExpr> exprs = ((PasProductExpr) expression).getExprList();
                if (exprs.size() > 1) {
                    return exprs.get(1).getText();
                }
            }
            return combineType(Operation.PRODUCT, ((PasProductExpr) expression).getExprList());
        } else if (expression instanceof PasCallExpr) {
            return inferCallType((PasCallExpr) expression);
        } else {
            List<PasField.ValueType> types = getTypes((PascalExpression) expression.getParent());
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

    private static String inferCallType(PasCallExpr expression) {
        PasFullyQualifiedIdent ref = PsiTreeUtil.findChildOfType(expression.getExpr(), PasFullyQualifiedIdent.class);
        if (null == ref) {
            return null;
        }
        Collection<PasField> routines = PasReferenceUtil.resolveExpr(null, NamespaceRec.fromElement(ref), PasField.TYPES_ROUTINE, true, 0);

        PascalRoutineImpl suitable = null;
        for (PasField routine : routines) {
            PascalNamedElement el = routine.getElement();
            if (el instanceof PascalRoutineImpl) {
                suitable = (PascalRoutineImpl) el;
                PasFormalParameterSection params = suitable.getFormalParameterSection();
                // TODO: make type check and handle overload
                if (params != null && params.getFormalParameterList().size() == expression.getArgumentList().getExprList().size()) {
                    return suitable.getFunctionTypeStr();
                }
            }
        }
        if (suitable != null) {
            return suitable.getFunctionTypeStr();
        }
        // Handle as typecast
        if ((expression.getExpr() instanceof PasReferenceExpr) && (expression.getArgumentList().getExprList().size() == 1)) {
            return expression.getExpr().getText();
        }
        return null;
    }

    private static String infereUnaryExprType(PasUnaryExpr expression) {
        PasUnaryOp op = expression.getUnaryOp();
        if ("@".equals(op.getText())) {
            return Primitive.POINTER.name;
        } else {
            return infereType(expression.getExpr());
        }
    }

    private static String combineType(Operation sum, List<PasExpr> exprList) {
        PasExpr arg1 = exprList.size() > 0 ? exprList.get(0) : null;
        PasExpr arg2 = exprList.size() > 1 ? exprList.get(1) : null;
        return arg1 != null ? infereType(arg1) : null;
    }

    @Nullable
    public static String getTypeIdentifier(PasField.ValueType type) {
        if (type.field != null) {
            PascalNamedElement el = type.field.getElement();
            if (el instanceof PasGenericTypeIdent) {
                return el.getText();
            } else {
                PasTypeDecl res = PsiUtil.getTypeDeclaration(type.field.getElement());
                return res != null ? res.getText() : null;
            }
        } else {
            return null;
        }
    }

    public static String calcAssignStatementType(PsiElement statement) {
        PasAssignPart assPart = PsiUtil.findImmChildOfAnyType(statement, PasAssignPart.class);
        return (assPart != null) && (assPart.getExpression() != null) ? infereType(assPart.getExpression().getExpr()) : null;
    }

    public enum Operation {
        SUM, PRODUCT
    }

    public enum Primitive {
        BYTE("Byte"), WORD("Word"), DWORD("DWord"), LONGWORD("Longword"),
        SHORTINT("Shortint"), SMALLINT("Smallint"), INTEGER("Integer"), LONGINT("Longint"), NATIVEINT("Nativeint"), INT64("Int64"),
        SINGLE("Single"), REAL("Real"), DOUBLE("Double"), EXTENDED("Extended"),
        POINTER("Pointer"), BOOLEAN("Boolean"), STRING("String");

        private final String name;

        Primitive(String name) {
            this.name = name;
        }
    }
}
