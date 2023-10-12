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
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StmtUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class AddCompoundStatement extends BaseElementAtCaretIntentionAction {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.statement.add.compound");
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
            return (stmt != null) && (stmt.getCompoundStatement() == null);
        } else {
            return false;
        }
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiElement parent = StmtUtil.getStatement(element);
        PasStatement stmt = StmtUtil.isStructuredOperatorStatement(parent) ? PsiTreeUtil.getChildOfType(parent, PasStatement.class) : null;
        if (PsiUtil.isElementUsable(stmt)) {
            PasCompoundStatement compoundStatement = PasElementFactory.createElementFromText(project, "begin end", PasCompoundStatement.class);
            if (compoundStatement != null) {
                List<PasStatement> stmts = compoundStatement.getStatementList();
                if (!stmts.isEmpty()) {
                    PsiElement oldStmt = stmt.copy();
                    stmts.get(0).replace(oldStmt);
                    stmt.replace(compoundStatement);
                }
            }
        }
    }

}
