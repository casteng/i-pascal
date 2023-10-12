package com.siberika.idea.pascal.lang.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.editor.highlighter.PascalReadWriteAccessDetector;
import com.siberika.idea.pascal.ide.actions.quickfix.IdentQuickFixes;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasExitStatement;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;

import java.util.Collection;
import java.util.List;

import static com.siberika.idea.pascal.PascalBundle.message;

public class ResultAssignmentInspection extends PascalLocalInspectionBase {
    @Override
    public void checkRoutine(PascalRoutine routine, ProblemsHolder holder, boolean isOnTheFly) {
        if (routine instanceof PasRoutineImplDecl) {
            if (routine.isFunction()) {
                PasCompoundStatement code = RoutineUtil.retrieveRoutineCodeBlock(routine);
                if (code != null) {
                    Collection<PascalPsiElement> elements = PsiTreeUtil.findChildrenOfAnyType(code, PasFullyQualifiedIdent.class, PasExitStatement.class);
                    for (PascalPsiElement element : elements) {
                        if (element instanceof PasFullyQualifiedIdent) {
                            List<PasSubIdent> subidents = ((PasFullyQualifiedIdent) element).getSubIdentList();
                            if (!subidents.isEmpty() && RoutineUtil.isFunctionResultReference(subidents.get(0), routine.getName())) {
                                if (PascalReadWriteAccessDetector.isWriteAccess(element)) {
                                    return;
                                }
                            }
                        } else if (PsiTreeUtil.getChildOfType(element, PasExpression.class) != null) {   // exit statement with result expression
                            return;
                        } else {                                                                         // exit statement without result expression
                            addWarning(holder, isOnTheFly, element);
                            return;
                        }
                    }
                    PsiElement end = code.getLastChild();
                    addWarning(holder, isOnTheFly, end);
                }
            }
        }
    }

    private void addWarning(ProblemsHolder holder, boolean isOnTheFly, PsiElement element) {
        holder.registerProblem(holder.getManager().createProblemDescriptor(element, message("inspection.warn.function.no.result.assignment"), true,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly,
                new IdentQuickFixes.AddResultAssignmentAction()));
    }

}
