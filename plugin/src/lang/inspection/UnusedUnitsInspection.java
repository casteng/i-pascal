package com.siberika.idea.pascal.lang.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.ModuleUtilCore;
import com.siberika.idea.pascal.ide.actions.UsesQuickFixes;
import com.siberika.idea.pascal.lang.PascalImportOptimizer;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;

import static com.siberika.idea.pascal.PascalBundle.message;

public class UnusedUnitsInspection extends PascalLocalInspectionBase {

    @Override
    public void checkUses(PasUsesClause usesClause, ProblemsHolder holder, boolean isOnTheFly) {
        for (PasNamespaceIdent usedUnit : usesClause.getNamespaceIdentList()) {
            ProblemDescriptor res = annotateUnit(holder.getManager(), usedUnit, isOnTheFly);
            if (res != null) {
                holder.registerProblem(res);
            }
        }

    }

    private ProblemDescriptor annotateUnit(InspectionManager holder, PascalQualifiedIdent usedUnitName, boolean isOnTheFly) {
        switch (PascalImportOptimizer.getUsedUnitStatus(usedUnitName, ModuleUtilCore.findModuleForPsiElement(usedUnitName))) {
            case UNUSED: {
                return holder.createProblemDescriptor(usedUnitName, message("inspection.warn.unused.unit"), true,
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly,
                        new UsesQuickFixes.RemoveUnitAction(), new UsesQuickFixes.ExcludeUnitAction(), new UsesQuickFixes.OptimizeUsesAction());
            }
            case USED_IN_IMPL: {
                return holder.createProblemDescriptor(usedUnitName, message("inspection.warn.unused.unit.interface"),
                        true, ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly,
                        new UsesQuickFixes.MoveUnitAction(), new UsesQuickFixes.RemoveUnitAction(), new UsesQuickFixes.ExcludeUnitAction(), new UsesQuickFixes.OptimizeUsesAction());
            }
        }
        return null;
    }

}
