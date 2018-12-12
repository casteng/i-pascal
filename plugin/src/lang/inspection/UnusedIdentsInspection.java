package com.siberika.idea.pascal.lang.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.siberika.idea.pascal.ide.actions.quickfix.IdentQuickFixes;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import static com.siberika.idea.pascal.PascalBundle.message;

public class UnusedIdentsInspection extends PascalLocalInspectionBase {

    private LocalSearchScope fileScope;

    @Override
    public void inspectionStarted(@NotNull LocalInspectionToolSession session, boolean isOnTheFly) {
        super.inspectionStarted(session, isOnTheFly);
        fileScope = new LocalSearchScope(session.getFile());
    }

    @Override
    public void checkNamedIdent(PascalNamedElement namedIdent, ProblemsHolder holder, boolean isOnTheFly) {
        ProblemDescriptor res = annotateIdent(holder.getManager(), namedIdent, isOnTheFly, fileScope);
        if (res != null) {
            holder.registerProblem(res);
        }
    }

    private ProblemDescriptor annotateIdent(InspectionManager holder, PascalNamedElement element, boolean isOnTheFly, LocalSearchScope fileScope) {
        Project project = element.getProject();
        if (element.isLocal() && !PsiUtil.isFormalParameterOfExportedRoutineOrProcType(element) && !PsiUtil.isPropertyIndexIdent(element)) {
            Query<PsiReference> usages = ReferencesSearch.search(element, fileScope);
            final boolean structDecl = PsiUtil.isStructDecl(element);
            final boolean method = PsiUtil.isRoutineName(element);
            if (usages.forEach(new Processor<PsiReference>() {
                @Override
                public boolean process(PsiReference psiReference) {
                    PsiElement el = psiReference.getElement();
                    if (PsiManager.getInstance(project).areElementsEquivalent(element, el) ||
                            ((structDecl || method) && (el instanceof PasSubIdent) && (el.getParent() instanceof PasClassQualifiedIdent)
                                    && (el.getParent().getParent() instanceof PasRoutineImplDecl))) {
                        return true;
                    } else {
                        return false;
                    }
                }
            })) {
                return holder.createProblemDescriptor(element, message("inspection.warn.unused.local.ident"), true,
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly,
                        new IdentQuickFixes.RemoveIdentAction(), new IdentQuickFixes.ExcludeIdentAction());
            }
        }
        return null;
    }

}
