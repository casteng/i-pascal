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
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.ide.actions.quickfix.IdentQuickFixes;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalInterfaceDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

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
        if (element.isLocal() && !PsiUtil.isFormalParameterOfExportedRoutineOrProcType(element) && !PsiUtil.isPropertyIndexIdent(element) && !isImplementedOrOverriddenRoutine(element)) {
            Query<PsiReference> usages = ReferencesSearch.search(element, fileScope);
            final boolean structDecl = PsiUtil.isStructDecl(element);
            final boolean method = PsiUtil.isRoutineName(element);
            if (usages.forEach(new Processor<PsiReference>() {
                @Override
                public boolean process(PsiReference psiReference) {
                    PsiElement el = psiReference.getElement();
                    return PsiManager.getInstance(project).areElementsEquivalent(element, el) ||
                            ((structDecl || method) && (el instanceof PasSubIdent) && (el.getParent() instanceof PasClassQualifiedIdent)
                                    && (el.getParent().getParent() instanceof PasRoutineImplDecl));
                }
            })) {
                return holder.createProblemDescriptor(element, message("inspection.warn.unused.local.ident"), true,
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly,
                        new IdentQuickFixes.RemoveIdentAction(), new IdentQuickFixes.ExcludeIdentAction());
            }
        }
        return null;
    }

    private boolean isImplementedOrOverriddenRoutine(PascalNamedElement element) {
        if (!((element.getParent() instanceof PasFormalParameter) && (element.getParent().getParent() instanceof PasFormalParameterSection)
                && element.getParent().getParent().getParent() instanceof PasRoutineImplDecl)) {
            return false;
        }
        PascalRoutine routine = (PascalRoutine) element.getParent().getParent().getParent();
        PasEntityScope scope = routine.getContainingScope();
        if (!(scope instanceof PasClassTypeDecl)) {
            return false;
        }
        PasExportedRoutine exportedRoutine;
        if (routine instanceof PasExportedRoutine) {
            exportedRoutine = (PasExportedRoutine) routine;
        } else {
            exportedRoutine = (PasExportedRoutine) SectionToggle.retrieveDeclaration(routine, true);
        }
        if (null == exportedRoutine) {
            return false;
        }
        if (exportedRoutine.isOverridden()) {
            return true;
        }
        final String methodName = exportedRoutine.getReducedName();
        return !processParents((PascalStructType) scope, new PsiElementProcessor<PasEntityScope>() {
            @Override
            public boolean execute(@NotNull PasEntityScope element) {
                if (element instanceof PascalInterfaceDecl) {
                    if (element.getRoutine(methodName) != null) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    private boolean processParents(PascalStructType scope, PsiElementProcessor<PasEntityScope> processor) {
        return processStruct(new SmartHashSet<>(), scope, processor, 0);
    }

    private boolean processStruct(Set<PascalStructType> processed, @NotNull PascalStructType struct, PsiElementProcessor<PasEntityScope> processor, int recursionCounter) {
        processed.add(struct);
        List<SmartPsiElementPointer<PasEntityScope>> parentScope = struct.getParentScope();
        for (SmartPsiElementPointer<PasEntityScope> parentPtr : parentScope) {
            PasEntityScope el = parentPtr.getElement();
            if ((el instanceof PascalStructType) && !processed.contains(el)) {
                processed.add((PascalStructType) el);
                if (!processor.execute(el)) {
                    return false;
                }
                if (!processStruct(processed, (PascalStructType) el, processor, recursionCounter + 1)) {
                    return false;
                }
            }
        }
        return true;
    }

}
