package com.siberika.idea.pascal.editor.formatter;

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasForStatement;
import com.siberika.idea.pascal.lang.psi.PasFromExpression;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasIfStatement;
import com.siberika.idea.pascal.lang.psi.PasParenExpr;
import com.siberika.idea.pascal.lang.psi.PasRepeatStatement;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasWhileStatement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalVariableDeclaration;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 17/02/2015
 */
public class PascalSmartEnterProcessor extends SmartEnterProcessor {

    @Override
    public boolean process(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        FeatureUsageTracker.getInstance().triggerFeatureUsed("codeassists.complete.statement");
        PsiElement atCursor = getStatementAtCaret(editor, psiFile);
        PascalPsiElement el = PsiTreeUtil.getParentOfType(atCursor, PasStatement.class, PasEntityScope.class, PascalExpression.class);
        if ((el instanceof PasStatement) || (el instanceof PascalExpression)) {
            completeStatement(editor, el);
        } else {
            el = PsiTreeUtil.getParentOfType(atCursor, PascalVariableDeclaration.class);
            if (el != null) {
                PascalCompleteIdent.completeIdent(editor, (PascalVariableDeclaration) el);
            }
        }
        if (el != null) {
            commitDocument(editor);
            CodeStyleManager.getInstance(el.getManager()).reformatRange(psiFile, el.getTextRange().getStartOffset(), el.getTextRange().getEndOffset(), true);
        }
        return true;
    }

    private static void completeStatement(Editor editor, PsiElement statement) {
        /** complete if, for, while, repeat, try, with
         *  complete ";" to EOL if needed
         *  complete ")" in calls where needed
         *  complete "end" if statement contains "begin" and existing "end" has less indent
         *  complete routines
         *  complete structured types
         *  remove ";" at EOL after "begin"
         */
        boolean processParent = false;
        if (statement instanceof PasWhileStatement) {
            completeWhile(editor, (PasWhileStatement) statement);
        } else if (statement instanceof PasForStatement) {
            completeFor(editor, (PasForStatement) statement);
        } else if (statement instanceof PasIfStatement) {
            completeIf(editor, (PasIfStatement) statement);
        } else if (statement instanceof PasRepeatStatement) {
            completeRepeat(editor, (PasRepeatStatement) statement);
        } else if ((statement instanceof PasCallExpr) || (statement instanceof PasParenExpr)) {
            processParent = !completeParen(editor, (PascalExpression) statement);
        } else if (statement instanceof PasStatement) {
            processParent = !completeSimpleStatement(editor, (PasStatement) statement);
        } else {
            processParent = true;
        }
        if (processParent) {
            if (statement instanceof PasStatement) {
                int ofs = statement.getTextRange().getEndOffset();
                ofs = DocUtil.expandRangeEnd(editor.getDocument(), ofs, DocUtil.RE_SEMICOLON);
                editor.getCaretModel().moveToOffset(ofs);
            } else {
                PascalPsiElement parent = PsiTreeUtil.getParentOfType(statement, PasStatement.class, PascalExpression.class);
                if (parent != null) {
                    completeStatement(editor, parent);
                }
            }
        }
    }

    private static boolean completeSimpleStatement(Editor editor, PasStatement outerStatement) {
        if (!DocUtil.isSingleLine(editor.getDocument(), outerStatement)) {
            PascalPsiElement stmt = PsiUtil.findImmChildOfAnyType(outerStatement, PasAssignPart.class);
            stmt = stmt != null ? stmt : PsiUtil.findImmChildOfAnyType(outerStatement, PasAssignPart.class, PasExpression.class);
            if (stmt != null) {
                int endStmt = stmt.getTextRange().getEndOffset();
                if (endStmt != outerStatement.getTextRange().getEndOffset()) {
                    DocUtil.adjustDocument(editor, endStmt, String.format(";%s", DocUtil.PLACEHOLDER_CARET));
                    return true;
                }
            }
        }
        return false;
    }

    /* WHILE
     . while [do] => while _ do
     . while expr [do] => while expr do \n _
     . while expr do _ => while expr do \n begin \n _ \n end;
     . while expr do _ \n stmt1; => while expr do \n _; stmt1;
     */
    private static final int LENGTH_WHILE = "while".length();

    private static void completeWhile(Editor editor, PasWhileStatement statement) {
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
            int offs = statement.getTextRange().getStartOffset() + LENGTH_WHILE;
            DocUtil.adjustDocument(editor, offs, " " + DocUtil.PLACEHOLDER_CARET + (doEnd >= 0 ? "" : " do"));
        }
        PsiDocumentManager.getInstance(statement.getProject()).commitDocument(editor.getDocument());
    }

    /* IF
     * if => if _ then
     * if expr [then] => if expr then \n _;
     * if expr then _ => if expr then \n begin \n _ \n end;
     * if expr then stmt _ => if expr then stmt else _
     * if expr then [begin] stmts [end] _ => if expr then [begin] stmts [end] ?\n else _
     * if expr then [begin] stmts [end] else _ => if expr then begin stmts end else \n begin \n _ \n end;
     */
    private static final int LENGTH_IF = "if".length();

    private static void completeIf(Editor editor, PasIfStatement statement) {
        int thenEnd = getChildEndOffset(statement, PasTypes.THEN);
        final PasExpression expr = statement.getExpression();
        boolean hasExpr = (expr != null) && atTheSameLine(editor.getDocument(), statement, expr);
        if (hasExpr) {
            if (thenEnd >= 0) {
                int caretPos = editor.getCaretModel().getCurrentCaret().getOffset();
                int elseEnd = getChildEndOffset(statement, PasTypes.ELSE);
                TextRange thenStmtRange = getElementRange(statement.getIfThenStatement());
                TextRange elseStmtRange = getElementRange(statement.getIfElseStatement());
                if ((thenStmtRange.getLength() > 0) && (elseEnd < 0) && (caretPos == thenStmtRange.getEndOffset())) {
                    DocUtil.adjustDocument(editor, caretPos, String.format("\nelse\n%s", DocUtil.PLACEHOLDER_CARET));
                } else if ((caretPos == thenEnd) && (thenStmtRange.getLength() == 0)) {
                    DocUtil.adjustDocument(editor, caretPos, String.format("\nbegin\n%s\nend", DocUtil.PLACEHOLDER_CARET));
                } else if ((caretPos == elseEnd) && (elseStmtRange.getLength() == 0)) {
                    DocUtil.adjustDocument(editor, caretPos, String.format("\nbegin\n%s\nend", DocUtil.PLACEHOLDER_CARET));
                } else if (caretPos < thenEnd) {
                    DocUtil.adjustDocument(editor, thenEnd, String.format("\n%s", DocUtil.PLACEHOLDER_CARET));
                } else if (caretPos < elseEnd) {
                    DocUtil.adjustDocument(editor, elseEnd, String.format("\n%s", DocUtil.PLACEHOLDER_CARET));
                }
            } else {
                int offs = expr.getTextRange().getEndOffset();
                DocUtil.adjustDocument(editor, offs, String.format(" then \n%s", DocUtil.PLACEHOLDER_CARET));
            }
            EditorUtil.moveToLineEnd(editor);
        } else {
            int offs = statement.getTextRange().getStartOffset() + LENGTH_IF;
            DocUtil.adjustDocument(editor, offs, " " + DocUtil.PLACEHOLDER_CARET + (thenEnd >= 0 ? "" : " then"));
        }
        PsiDocumentManager.getInstance(statement.getProject()).commitDocument(editor.getDocument());
    }

    /* REPEAT
     * repeat => repeat \n _ \n until
     * repeat until [;] => repeat \n until _;
     */
    private static final int LENGTH_REPEAT = "repeat".length();

    private static void completeRepeat(Editor editor, PasRepeatStatement statement) {
        int untilEnd = getChildEndOffset(statement, PasTypes.UNTIL);
        if (untilEnd < 0) {
            DocUtil.adjustDocument(editor, statement.getTextRange().getStartOffset() + LENGTH_REPEAT,
                    String.format("\n%s\nuntil;", DocUtil.PLACEHOLDER_CARET));
            EditorUtil.moveToLineEnd(editor);
        } else if (statement.getExpression() == null) {
            DocUtil.adjustDocument(editor, untilEnd, String.format(" %s;", DocUtil.PLACEHOLDER_CARET));
        }
        PsiDocumentManager.getInstance(statement.getProject()).commitDocument(editor.getDocument());
    }

    private static TextRange getElementRange(PsiElement element) {
        return element != null ? element.getTextRange() : TextRange.EMPTY_RANGE;
    }

    /* FOR
     * for => for _ do
     * for i [to] [do] => for i := _ [to] [do] // if i is 1-letter in length
     * for [i] to [do] => for i := _ to [do]
     * for i := [to] [do] => for i := _ to do
     * for i := 0 [to] [do] => for i := 0 to _ do
     * for i := 0 to 1 [do] => for i := 0 to 1 do \n _
     * for in [do] => for _ in [do]
     * for i in [do] => for i in _ do
     * for i in x [do] => for i in x do \n _
     ? for x [do] => for i in x do \n _ // for enumerable types of x
     */
    private static final int LENGTH_FOR = "for".length();

    private static void completeFor(Editor editor, PasForStatement statement) {
        int assignEnd = getChildEndOffset(PasTypes.ASSIGN, statement, statement.getStatement());
        int inEnd = getChildEndOffset(PasTypes.IN, statement, statement.getStatement());
        PsiElement errorEl = PsiTreeUtil.findChildOfType(statement.getStatement(), PsiErrorElement.class);
        int toEnd = getChildEndOffset(PasTypes.TO, statement, statement.getStatement(), errorEl);
        toEnd = toEnd < 0 ? getChildEndOffset(PasTypes.DOWNTO, statement, statement.getStatement(), errorEl) : toEnd;
        int doEnd = getChildEndOffset(PasTypes.DO, statement, statement.getStatement());
        final PasFullyQualifiedIdent ident = statement.getFullyQualifiedIdent();
        boolean hasIdent = (ident != null) && atTheSameLine(editor.getDocument(), statement, ident);
        final PasFromExpression fromExpr = statement.getFromExpression();
        final PasExpression expr = statement.getExpression();
        boolean hasExpr = (expr != null) && atTheSameLine(editor.getDocument(), statement, expr);
        String doStr = doEnd >= 0 ? "" : " do";
        if (hasIdent) {
            String toStr = toEnd >= 0 ? "" : " to";
            if (assignEnd < 0) {
                if (inEnd < 0) {
                    if ((ident.getTextLength() == 1) || (toEnd >= 0)) {
                        DocUtil.adjustDocument(editor, ident.getTextRange().getEndOffset(), " := " + DocUtil.PLACEHOLDER_CARET + toStr + doStr);
                    }
                } else if (!hasExpr) {
                    DocUtil.adjustDocument(editor, inEnd, " " + DocUtil.PLACEHOLDER_CARET + doStr);
                } else {
                    insertNewLine(editor, doStr, doEnd >= 0 ? doEnd : expr.getTextRange().getEndOffset());
                }
            } else if (null == fromExpr) {
                DocUtil.adjustDocument(editor, assignEnd, " " + DocUtil.PLACEHOLDER_CARET + toStr + doStr);
            } else if (toEnd < 0) {
                DocUtil.adjustDocument(editor, fromExpr.getTextRange().getEndOffset(), " to " + DocUtil.PLACEHOLDER_CARET + doStr);
            } else if (!hasExpr) {
                DocUtil.adjustDocument(editor, toEnd, " " + DocUtil.PLACEHOLDER_CARET + doStr);
            } else {
                insertNewLine(editor, doStr, doEnd >= 0 ? doEnd : expr.getTextRange().getEndOffset());
            }
        } else {
            int offs = statement.getTextRange().getStartOffset() + LENGTH_FOR;
            DocUtil.adjustDocument(editor, offs, " " + DocUtil.PLACEHOLDER_CARET + (inEnd < 0 ? doStr : ""));
        }
        PsiDocumentManager.getInstance(statement.getProject()).commitDocument(editor.getDocument());
    }

    /* EXPRESSION
     * ([x] => ([x]_)
     * func([x] => func([x]_)
     */
    private static boolean completeParen(Editor editor, PascalExpression statement) {
        int parenPos = getChildEndOffset(PasTypes.RPAREN, statement, statement.getLastChild());
        if (parenPos < 0) {
            int ofs = DocUtil.isSingleLine(editor.getDocument(), statement) ? statement.getTextRange().getEndOffset() : getVisualLineEnd(editor) - 1;
            DocUtil.adjustDocument(editor, ofs, DocUtil.PLACEHOLDER_CARET + ")");
            PsiDocumentManager.getInstance(statement.getProject()).commitDocument(editor.getDocument());
            return true;
        }
        return false;
    }

    private static void insertNewLine(Editor editor, String doStr, int offset) {
        DocUtil.adjustDocument(editor, offset, doStr + "\n" + DocUtil.PLACEHOLDER_CARET);
        int caretPos = editor.getCaretModel().getCurrentCaret().getOffset();
        int lineEndPos = getVisualLineEnd(editor) - 1;
        if (caretPos >= lineEndPos) {
            EditorUtil.moveToLineEnd(editor);
        }
    }

    private static int getVisualLineEnd(Editor editor) {
        return editor.getCaretModel().getCurrentCaret().getVisualLineEnd();
    }

    private static int getChildEndOffset(PsiElement element, IElementType type) {
        ASTNode child = element.getNode().findChildByType(type);
        return child != null ? child.getTextRange().getEndOffset() : -1;
    }

    static int getChildEndOffset(IElementType type, PsiElement...elements) {
        for (PsiElement element : elements) if (element != null) {
            ASTNode child = element.getNode().findChildByType(type);
            if (child != null) {
                return child.getTextRange().getEndOffset();
            }
        }
        return -1;
    }

    private static boolean atTheSameLine(Document doc, PsiElement first, PsiElement second) {
        return doc.getLineNumber(first.getTextRange().getStartOffset()) == doc.getLineNumber(second.getTextRange().getStartOffset());
    }

}
