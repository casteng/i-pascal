package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Highlight all identifiers in WITH statement which come from the scope under cursor or all identifiers from WITH scopes when cursor is on WITH keyword
 */
public class PasHighlightWithIdentsHandler extends HighlightUsagesHandlerBase<PsiElement> {
    private final PsiElement target;

    PasHighlightWithIdentsHandler(Editor editor, PsiFile file, PsiElement target) {
        super(editor, file);
        this.target = target;
    }

    @Override
    public List<PsiElement> getTargets() {
        return Collections.singletonList(target);
    }

    @Override
    protected void selectTargets(List<PsiElement> targets, Consumer<List<PsiElement>> selectionConsumer) {
        selectionConsumer.consume(targets);
    }

    @Override
    public void computeUsages(List<PsiElement> targets) {
        PasWithStatement with;
        PsiElement expr = null;
        if (target.getParent() instanceof PasWithStatement) {
            with = (PasWithStatement) target.getParent();
        } else {
            with = PascalHighlightHandlerFactory.getWithStatement(target);
            if (null == with) {
                return;
            }
            expr = PsiUtil.skipToExpression(target.getParent());
            expr = expr != null ? expr.getParent() : null;
            if (expr != null && expr.getParent() != with) {
                expr = null;
            }
            addOccurrence(target);
        }

        addOccurrence(with.getFirstChild());

        addOccurrence(target);
        Collection<PasFullyQualifiedIdent> idents = PsiTreeUtil.findChildrenOfAnyType(with, PasFullyQualifiedIdent.class);
        for (PasFullyQualifiedIdent ident : idents) {
            if (expr instanceof PasExpression) {
                processElementsFromWith((PasExpression) expr, ident, element -> {
                    addOccurrence(element);
                    return true;
                });
            } else {
                for (PasExpression withExpr : with.getExpressionList()) {
                    processElementsFromWith(withExpr, ident, element -> {
                        addOccurrence(element);
                        return true;
                    });
                }
            }
        }
    }

    public static void processElementsFromWith(PasExpression withExpr, PasFullyQualifiedIdent namedElement, PsiElementProcessor<PasSubIdent> processor) {
        PasExpr expression = withExpr != null ? withExpr.getExpr() : null;
        if (expression instanceof PascalExpression) {
            List<PasField.ValueType> types = PascalExpression.getTypes((PascalExpression) withExpr.getExpr());
            if (!types.isEmpty()) {
                PasEntityScope ns = PascalExpression.retrieveScope(types);
                if (ns instanceof PascalStructType) {
                    processWithNamespace(ns, namedElement, processor);
                    for (SmartPsiElementPointer<PasEntityScope> scopePtr : ns.getParentScope()) {
                        processWithNamespace(scopePtr.getElement(), namedElement, processor);
                    }
                }
            }
        }
    }

    private static void processWithNamespace(PasEntityScope scope, PasFullyQualifiedIdent namedElement, PsiElementProcessor<PasSubIdent> processor) {
        List<PasSubIdent> subidents = namedElement.getSubIdentList();
        if (!subidents.isEmpty()) {
            PasSubIdent sub = subidents.get(0);
            if (scope instanceof PascalStructType) {
                PasField field = scope.getField(sub.getName());
                if (field != null) {
                    processor.execute(sub);
                }
            }
        }
    }

}
