package com.siberika.idea.pascal.editor.formatter;

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasForStatement;
import com.siberika.idea.pascal.lang.psi.PasIfStatement;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasWhileStatement;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 17/02/2015
 */
public class PascalSmartEnterProcessor extends SmartEnterProcessor {
    @Override
    public boolean process(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        FeatureUsageTracker.getInstance().triggerFeatureUsed("codeassists.complete.statement");
        PsiElement el = getStatementAtCaret(editor, psiFile);
        el = PsiTreeUtil.getParentOfType(el, PasStatement.class, PasEntityScope.class);
        if (el instanceof PasStatement) {
            completeStatement(editor, el);
        }
        return true;
    }

    private void completeStatement(Editor editor, PsiElement statement) {
        /** complete if, for, while, repeat, try, with
         *  complete ";" to EOL if needed
         *  complete ")" in calls where needed
         *  complete "end" if statement contains "begin" and existing "end" has less indent
         *  complete routines
         *  complete structured types
         *  remove ";" at EOL after "begin"
         */
        if (statement instanceof PasWhileStatement) {
            completeWhile(editor, (PasWhileStatement) statement);
        } else if (statement instanceof PasForStatement) {
            completeIf(editor, statement);
        } else if (statement instanceof PasIfStatement) {
            completeIf(editor, statement);
        } else if (statement instanceof PasIfStatement) {
            completeIf(editor, statement);
        } else if (statement instanceof PasIfStatement) {
            completeIf(editor, statement);
        } else if (statement instanceof PasIfStatement) {
            completeIf(editor, statement);
        }
        CodeStyleManager.getInstance(statement.getManager()).reformat(statement, true);
    }

    // while [do] => while _ do
    // while expr [do] => while expr do \n _
    // while expr do _ => while expr do \n begin \n _ \n end;
    // while expr do _ \n stmt1; => while expr do \n _; stmt1;
    private void completeWhile(Editor editor, PasWhileStatement statement) {
        int doEnd = getChildEndOffset(statement, PasTypes.DO);
        final PasExpression expr = statement.getExpression();
        boolean hasExpr = (expr != null) && atTheSameLine(editor.getDocument(), statement, expr);
        if (hasExpr) {
            if (doEnd >= 0) {
                if (editor.getCaretModel().getCurrentCaret().getOffset() == doEnd) {
                    DocUtil.adjustDocument(editor, doEnd, String.format("\nbegin\n%s\nend;", DocUtil.PLACEHOLDER_CARET));
                } else {
                    DocUtil.adjustDocument(editor, doEnd, String.format("\n%s", DocUtil.PLACEHOLDER_CARET));
                }
            } else {
                int offs = expr.getTextRange().getEndOffset();
                DocUtil.adjustDocument(editor, offs, String.format(" do \n%s", DocUtil.PLACEHOLDER_CARET));
            }
            EditorUtil.moveToLineEnd(editor);
        } else {
            int offs = statement.getTextRange().getStartOffset() + 5;
            DocUtil.adjustDocument(editor, offs, " " + DocUtil.PLACEHOLDER_CARET + (doEnd >= 0 ? "" : " do"));
        }
        PsiDocumentManager.getInstance(statement.getProject()).commitDocument(editor.getDocument());
    }

    private static int getChildEndOffset(PsiElement element, IElementType type) {
        ASTNode child = element.getNode().findChildByType(type);
        return child != null ? child.getTextRange().getEndOffset() : -1;
    }

    private static boolean atTheSameLine(Document doc, PsiElement first, PsiElement second) {
        return doc.getLineNumber(first.getTextRange().getStartOffset()) == doc.getLineNumber(second.getTextRange().getStartOffset());
    }

    private void completeIf(Editor editor, PsiElement statement) {

    }

}
