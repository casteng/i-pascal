package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasIndexExpr;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasProductExpr;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
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

    public static List<PasField.ValueType> getType(PascalExpression expr) throws PasInvalidScopeException {
        List<PasField.ValueType> res;

        if (expr instanceof PasReferenceExpr) {
            res = getChildType(getFirstChild(expr));
            final Collection<PasField> references = PasReferenceUtil.resolve(retrieveScope(res),
                    NamespaceRec.fromElement(((PasReferenceExpr) expr).getFullyQualifiedIdent()), PasField.TYPES_ALL, true, 0);
            if (!references.isEmpty()) {
                PasField field = references.iterator().next();
                PasReferenceUtil.retrieveFieldTypeScope(field);
                PasField.ValueType fieldType = field.getValueType();
                if (fieldType != null) {
                    res.add(fieldType);
                }
            }
        } else if (expr instanceof PasDereferenceExpr) {
            res = getChildType(getFirstChild(expr));
            res.add(new PasField.ValueType(null, PasField.Kind.POINTER, null, null));
        } else if (expr instanceof PasIndexExpr) {
            res = getChildType(getFirstChild(expr));
            res.add(new PasField.ValueType(null, PasField.Kind.ARRAY, null, null));
        } else if (expr instanceof PasProductExpr) {                                      // AS operator case
            res = getChildType(getLastChild(expr));
        } else {
            res = getChildType(getFirstChild(expr));
        }

        return res;
    }

    private static List<PasField.ValueType> getChildType(PsiElement child) throws PasInvalidScopeException {
        if (child instanceof PascalExpression) {
            return PascalExpression.getType((PascalExpression) child);
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

}
