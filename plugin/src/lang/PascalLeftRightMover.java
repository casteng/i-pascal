package com.siberika.idea.pascal.lang;

import com.intellij.codeInsight.editorActions.moveLeftRight.MoveElementLeftRightHandler;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasCaseItem;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasGenericDefinition;
import com.siberika.idea.pascal.lang.psi.PasIndexList;
import com.siberika.idea.pascal.lang.psi.PasProductExpr;
import com.siberika.idea.pascal.lang.psi.PasRelationalExpr;
import com.siberika.idea.pascal.lang.psi.PasSumExpr;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PasVarDeclaration;
import org.jetbrains.annotations.NotNull;

public class PascalLeftRightMover extends MoveElementLeftRightHandler {
    @NotNull
    @Override
    public PsiElement[] getMovableSubElements(@NotNull PsiElement element) {
        if (element instanceof PasFormalParameter) {
            return ((PasFormalParameter) element).getNamedIdentList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasFormalParameterSection) {
            return ((PasFormalParameterSection) element).getFormalParameterList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasArgumentList) {
            return ((PasArgumentList) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasEnumType) {
            return ((PasEnumType) element).getNamedIdentDeclList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasGenericDefinition) {
            return ((PasGenericDefinition) element).getNamedIdentList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasClassParent) {
            return ((PasClassParent) element).getTypeIDList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasUsesClause) {
            return ((PasUsesClause) element).getNamespaceIdentList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasClassField) {
            return ((PasClassField) element).getNamedIdentDeclList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasVarDeclaration) {
            return ((PasVarDeclaration) element).getNamedIdentDeclList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasCaseItem) {
            return ((PasCaseItem) element).getConstExpressionOrdList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasSumExpr) {
            return ((PasSumExpr) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasRelationalExpr) {
            return ((PasRelationalExpr) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasProductExpr) {
            return ((PasProductExpr) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasIndexList) {
            return ((PasIndexList) element).getExprList().toArray(PsiElement.EMPTY_ARRAY);
        } else if (element instanceof PasCompoundStatement) {
            return ((PasCompoundStatement) element).getStatementList().toArray(PsiElement.EMPTY_ARRAY);
        }
        return PsiElement.EMPTY_ARRAY;
    }
}
