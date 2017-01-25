package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasIndexExpr;
import com.siberika.idea.pascal.lang.psi.PasLiteralExpr;
import com.siberika.idea.pascal.lang.psi.PasProductExpr;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasTypes;
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

    public static String infereType(PascalExpression expression) {
        PsiElement expr = expression.getFirstChild();
        if (expr instanceof PasReferenceExpr) {
            List<PasField.ValueType> types = getTypes((PascalExpression) expr);
            for (PasField.ValueType type : types) {
                if (type.field != null) {
                    return PsiUtil.getTypeDeclaration(type.field.getElement()).getText();
                }
            }
        } else if (expr instanceof PasLiteralExpr) {
            PsiElement literal = expr.getFirstChild();
            IElementType type = literal.getNode().getElementType();
            if ((type == PasTypes.NUMBER_INT) || (type == PasTypes.NUMBER_HEX) || (type == PasTypes.NUMBER_OCT) || (type == PasTypes.NUMBER_BIN)) {
                return "Integer";
            } else if (type == PasTypes.NUMBER_REAL) {
                return "Single";
            } else if ((type == PasTypes.TRUE) || (type == PasTypes.FALSE)) {
                return "Boolean";
            } else if (type == PasTypes.NIL) {
                return "Pointer";
            } else if (type == PasTypes.STRING_FACTOR) {
                return "String";
            }
        }
        return null;
    }
}
