package com.siberika.idea.pascal.lang.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static com.siberika.idea.pascal.PascalBundle.message;

public class UnusedIdentsInspection extends LocalInspectionTool {
    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        List<ProblemDescriptor> result = new SmartList<>();
        PsiElement impl;
        if (file instanceof PascalFile) {
            impl = ((PascalFile) file).getImplementationSection();
        } else {
            impl = PsiUtil.getModuleImplementationSection(file);
        }
        LocalSearchScope fileScope = new LocalSearchScope(file);
        Collection<PascalNamedElement> idents = PsiTreeUtil.findChildrenOfAnyType(impl, PasNamedIdentDecl.class, PasNamedIdent.class);
        for (PascalNamedElement ident : idents) {
            ProblemDescriptor res = annotateIdent(manager, ident, isOnTheFly, fileScope);
            if (res != null) {
                result.add(res);
            }
        }
        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    private ProblemDescriptor annotateIdent(InspectionManager holder, PascalNamedElement element, boolean isOnTheFly, LocalSearchScope fileScope) {
        if (element.isLocal() && !PsiUtil.isFormalParameterOfExportedRoutine(element) && !PsiUtil.isPropertyIndexIdent(element)) {
            Query<PsiReference> usages = ReferencesSearch.search(element, fileScope);
            final boolean structDecl = PsiUtil.isStructDecl(element);
            if (usages.forEach(new Processor<PsiReference>() {
                @Override
                public boolean process(PsiReference psiReference) {
                    PsiElement el = psiReference.getElement();
                    if (structDecl && (el instanceof PasSubIdent) && (el.getParent() instanceof PasClassQualifiedIdent)
                            && (el.getParent().getParent() instanceof PasRoutineImplDecl)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            })) {
                return holder.createProblemDescriptor(element, message("inspection.unused.local.ident"), true,
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly);
            }
        }
        return null;
    }

}
