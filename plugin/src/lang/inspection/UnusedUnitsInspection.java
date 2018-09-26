package com.siberika.idea.pascal.lang.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.ide.actions.UsesQuickFixes;
import com.siberika.idea.pascal.lang.PascalImportOptimizer;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.siberika.idea.pascal.PascalBundle.message;

public class UnusedUnitsInspection extends LocalInspectionTool {
    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        List<ProblemDescriptor> result = new SmartList<>();
        List<PascalQualifiedIdent> usedUnits = PsiUtil.getUsedUnits(file);
        for (PascalQualifiedIdent usedUnit : usedUnits) {
            ProblemDescriptor res = annotateUnit(manager, usedUnit, isOnTheFly);
            if (res != null) {
                result.add(res);
            }
        }
        return result.toArray(new ProblemDescriptor[0]);
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
