package com.siberika.idea.pascal.lang.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.ide.actions.quickfix.IdentQuickFixes;
import com.siberika.idea.pascal.lang.psi.PasBlockBody;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasProcBodyBlock;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.util.PsiUtil;

import static com.siberika.idea.pascal.PascalBundle.message;

public class DestructorInheritedInspection extends PascalLocalInspectionBase {

    @Override
    public void checkRoutine(PascalRoutine routine, ProblemsHolder holder, boolean isOnTheFly) {
        if (routine instanceof PasRoutineImplDecl) {
            if (RoutineUtil.isDestructor(routine)) {
                PasProcBodyBlock block = ((PasRoutineImplDecl) routine).getProcBodyBlock();
                PasBlockLocal blockLocal = block != null ? block.getBlockLocal() : null;
                PasBlockBody blockBody = blockLocal != null ? blockLocal.getBlockBody() : null;
                PasCompoundStatement code = blockBody != null ? blockBody.getCompoundStatement() : null;
                if (code != null) {
                    PsiElement inherited = PsiUtil.findChildByElementType(code, PasTypes.INHERITED);

                    if (null == inherited) {
                        PsiElement end = code.getLastChild();
                        holder.registerProblem(holder.getManager().createProblemDescriptor(end, message("inspection.warn.destructor.no.inherited"), true,
                                ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly,
                                new IdentQuickFixes.addInheritedAction()));
                    }
                }
            }
        }
    }
}
