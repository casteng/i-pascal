package com.siberika.idea.pascal.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.psi.PasCaseElse;
import com.siberika.idea.pascal.lang.psi.PasCaseItem;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasForStatement;
import com.siberika.idea.pascal.lang.psi.PasHandler;
import com.siberika.idea.pascal.lang.psi.PasIfElseStatement;
import com.siberika.idea.pascal.lang.psi.PasIfStatement;
import com.siberika.idea.pascal.lang.psi.PasIfThenStatement;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasWhileStatement;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;

public class StmtUtil {
    private static final TokenSet TOKENS_STRUCTURED_STATEMENT = TokenSet.create(PasTypes.BEGIN, PasTypes.DO, PasTypes.FOR,
            PasTypes.IF, PasTypes.THEN, PasTypes.ELSE, PasTypes.WHILE, PasTypes.WITH, PasTypes.ON, PasTypes.CONST_EXPRESSION_ORD);

    public static PsiElement getStatement(PsiElement element) {
                PsiElement parent = null;
                IElementType type = element.getNode().getElementType();
                if (TOKENS_STRUCTURED_STATEMENT.contains(type)) {
                    parent = element.getParent();
                    if (parent instanceof PasIfStatement) {
                        parent = (type != PasTypes.ELSE) ? ((PasIfStatement) parent).getIfThenStatement() : ((PasIfStatement) parent).getIfElseStatement();
                    } else if ((type == PasTypes.BEGIN) && (parent instanceof PasCompoundStatement)) {
                        parent = parent.getParent().getParent();
                    }
                }
                return parent;
            }

    public static boolean isStructuredOperatorStatement(PsiElement parent) {
        return PsiUtil.isInstanceOfAny(parent, PasIfThenStatement.class, PasIfElseStatement.class, PasWhileStatement.class, PasForStatement.class,
                PasWithStatement.class, PasHandler.class, PasCaseItem.class, PasCaseElse.class);
    }

    // returns element before which an assignment statement can be placed without breaking code structure
    public static PsiElement findAssignmentLocation(PsiElement element) {
        while ((element != null) && !(element instanceof PasStatement)) {
            element = element.getParent();
        }
        if (element != null) {             // Check if the statement is not child of a single-statement structured operator
            PsiElement parent = element.getParent();
            while (isStructuredOperatorStatement(parent)) {
                parent = parent.getParent();
                element = parent;
            }
        }
        return element;
    }
}
