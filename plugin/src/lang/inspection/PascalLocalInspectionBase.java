package com.siberika.idea.pascal.lang.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PasVisitor;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import org.jetbrains.annotations.NotNull;

public abstract class PascalLocalInspectionBase extends LocalInspectionTool {
    public void checkUses(PasUsesClause usesClause, ProblemsHolder holder, boolean isOnTheFly) {
    }

    public void checkRoutine(PascalRoutine routine, ProblemsHolder holder, boolean isOnTheFly) {
    }

    public void checkNamedIdent(PascalNamedElement namedIdent, ProblemsHolder holder, boolean isOnTheFly) {
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new PasVisitor() {

            @Override
            public void visitUsesClause(@NotNull PasUsesClause usesClause) {
                checkUses(usesClause, holder, isOnTheFly);
            }

            @Override
            public void visitNamedIdent(@NotNull PasNamedIdent namedIdent) {
                checkNamedIdent(namedIdent, holder, isOnTheFly);
            }

            @Override
            public void visitcalRoutine(@NotNull PascalRoutine routine) {
                checkRoutine(routine, holder, isOnTheFly);
            }

        };
    }
}
