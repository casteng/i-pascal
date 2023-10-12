package com.siberika.idea.pascal.ide.intention;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasStmtEmpty;
import com.siberika.idea.pascal.util.StmtUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class RemoveCompoundStatement extends BaseElementAtCaretIntentionAction {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.statement.remove.compound");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "Statement/" + getClass().getSimpleName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiElement parent = StmtUtil.getStatement(element);
        if (StmtUtil.isStructuredOperatorStatement(parent)) {
            PasStatement stmt = PsiTreeUtil.getChildOfType(parent, PasStatement.class);
            if (stmt != null) {
                PasCompoundStatement compoundStatement = stmt.getCompoundStatement();
                return (compoundStatement != null) && isSingleOrEmptyCompoundStatement(compoundStatement);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiElement parent = StmtUtil.getStatement(element);
        PasStatement stmt = StmtUtil.isStructuredOperatorStatement(parent) ? PsiTreeUtil.getChildOfType(parent, PasStatement.class) : null;
        PasCompoundStatement compoundStatement = stmt != null ? stmt.getCompoundStatement() : null;
        PasStatement statement;
        if ((compoundStatement != null) && isSingleOrEmptyCompoundStatement(compoundStatement)) {
            statement = !compoundStatement.getStatementList().isEmpty() ? compoundStatement.getStatementList().get(0) : null;
            if (statement != null) {
                compoundStatement.replace(statement);
            } else {
                compoundStatement.delete();
            }
        }
    }

    private static boolean isSingleOrEmptyCompoundStatement(PasCompoundStatement statement) {
        List<PasStatement> list = statement.getStatementList();
        return (list.size() < 2) || ((list.size() == 2) && (list.get(1).getFirstChild() instanceof PasStmtEmpty));
    }

}
