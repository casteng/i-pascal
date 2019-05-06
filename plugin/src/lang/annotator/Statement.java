package com.siberika.idea.pascal.lang.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasCaseElse;
import com.siberika.idea.pascal.lang.psi.PasCaseItem;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasForStatement;
import com.siberika.idea.pascal.lang.psi.PasHandler;
import com.siberika.idea.pascal.lang.psi.PasIfElseStatement;
import com.siberika.idea.pascal.lang.psi.PasIfStatement;
import com.siberika.idea.pascal.lang.psi.PasIfThenStatement;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasStmtEmpty;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasWhileStatement;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Statement {

    private static final TokenSet TOKENS_STRUCTURED_STATEMENT = TokenSet.create(PasTypes.BEGIN, PasTypes.DO, PasTypes.FOR,
            PasTypes.IF, PasTypes.THEN, PasTypes.ELSE, PasTypes.WHILE, PasTypes.WITH, PasTypes.ON, PasTypes.CONST_EXPRESSION_ORD);

    public static void annotate(PsiElement element, AnnotationHolder holder) {
        PsiElement parent = getStatement(element);
        if (PsiUtil.isInstanceOfAny(parent, PasIfThenStatement.class, PasIfElseStatement.class, PasWhileStatement.class, PasForStatement.class,
                PasWithStatement.class, PasHandler.class, PasCaseItem.class, PasCaseElse.class)) {
            PasStatement stmt = PsiTreeUtil.getChildOfType(parent, PasStatement.class);
            if (null == stmt) {
                return;
            }
            PasCompoundStatement compoundStatement = stmt.getCompoundStatement();
            if (compoundStatement != null) {
                if (isSingleOrEmptycompoundStatement(compoundStatement)) {
                    Annotation ann = holder.createInfoAnnotation(element, PascalBundle.message("action.fix.statement.remove.compound"));
                    ann.registerFix(new RemoveCompoundStatementFix(stmt));
                }
            } else {
                Annotation ann = holder.createInfoAnnotation(element, PascalBundle.message("action.fix.statement.add.compound"));
                ann.registerFix(new AddCompoundStatementFix(stmt));
            }
        }
    }

    private static PsiElement getStatement(PsiElement element) {
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

    private static boolean isSingleOrEmptycompoundStatement(PasCompoundStatement statement) {
        List<PasStatement> list = statement.getStatementList();
        return (list.size() < 2) || ((list.size() == 2) && (list.get(1).getFirstChild() instanceof PasStmtEmpty));
    }

    private static class RemoveCompoundStatementFix extends FixBase<PasStatement> {
        RemoveCompoundStatementFix(PasStatement element) {
            super(element);
        }

        @NotNull
        @Override
        public String getText() {
            return PascalBundle.message("action.fix.statement.remove.compound");
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            PasCompoundStatement compoundStatement = element.getCompoundStatement();
            PasStatement statement;
            if ((compoundStatement != null) && isSingleOrEmptycompoundStatement(compoundStatement)) {
                statement = !compoundStatement.getStatementList().isEmpty() ? compoundStatement.getStatementList().get(0) : null;
                ApplicationManager.getApplication().runWriteAction(
                        () -> {
                            if (statement != null) {
                                compoundStatement.replace(statement);
                            } else {
                                compoundStatement.delete();
                            }
                        }
                );
            }
        }
    }

    private static class AddCompoundStatementFix extends FixBase<PasStatement> {
        AddCompoundStatementFix(PasStatement element) {
            super(element);
        }

        @NotNull
        @Override
        public String getText() {
            return PascalBundle.message("action.fix.statement.add.compound");
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            if (PsiUtil.isElementUsable(element)) {
                ApplicationManager.getApplication().runWriteAction(
                        () -> {
                            PasCompoundStatement compoundStatement = PasElementFactory.createElementFromText(project, "begin end", PasCompoundStatement.class);
                            if (compoundStatement != null) {
                                List<PasStatement> stmts = compoundStatement.getStatementList();
                                if (!stmts.isEmpty()) {
                                    PsiElement oldStmt = element.copy();
                                    stmts.get(0).replace(oldStmt);
                                    element.replace(compoundStatement);
                                }
                            }
                        }
                );
            }
        }
    }

}
