package com.siberika.idea.pascal.ide.actions.quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.PascalDocumentationProvider;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasExitStatement;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasVarDeclaration;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.siberika.idea.pascal.PascalBundle.message;

public class IdentQuickFixes {

    public static class ExcludeIdentAction extends PascalBaseFix {

        @Nls
        @NotNull
        @Override
        public String getName() {
            return message("action.ident.exclude");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement usedUnitName = descriptor.getPsiElement();
            if (null == usedUnitName) {
                return;
            }
            usedUnitName.addAfter(PasElementFactory.createElementFromText(usedUnitName.getProject(), "{!}"), null);
        }
    }

    public static class RemoveIdentAction extends PascalBaseFix {

        @Nls
        @NotNull
        public String getName() {
            return message("action.ident.remove");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement identName = descriptor.getPsiElement();
            List<PsiElement> toRemove = new SmartList<>();
            PsiElement parent = identName.getParent();
            parent = parent instanceof PasGenericTypeIdent ? parent.getParent() : parent;
            if (parent instanceof PasVarDeclaration) {
                toRemove.add(identName);
                if (((PasVarDeclaration) parent).getNamedIdentDeclList().size() <= 1) {
                    toRemove.add(parent);
                    PsiElement section = parent.getParent();
                    if (section instanceof PasVarSection) {
                        if (((PasVarSection) section).getVarDeclarationList().size() <= 1) {
                            toRemove.add(section);
                        }
                    }
                }
            } else if ((parent instanceof PasConstDeclaration) || (parent instanceof PasTypeDeclaration)) {
                toRemove.add(parent);
                PsiElement section = parent.getParent();
                if (section instanceof PasConstSection) {
                    if (((PasConstSection) section).getConstDeclarationList().size() <= 1) {
                        toRemove.add(section);
                    }
                } else if (section instanceof PasTypeSection) {
                    if (((PasTypeSection) section).getTypeDeclarationList().size() <= 1) {
                        toRemove.add(section);
                    }
                }
            } else if (parent instanceof PascalRoutine) {
                toRemove.add(parent);
                PsiElement decl = SectionToggle.getImplementationOrDeclaration((PascalRoutine) parent);
                if (decl != null) {
                    toRemove.add(decl);
                }
                toRemove.addAll(PascalDocumentationProvider.findElementCommentElements(identName.getContainingFile(), parent));
            }
            toRemove.addAll(PascalDocumentationProvider.findElementCommentElements(identName.getContainingFile(), identName));
            if (!addIfComma(toRemove, identName.getNextSibling())) {
                PsiElement prev = PsiTreeUtil.skipSiblingsBackward(identName, PsiUtil.ELEMENT_WS_COMMENTS);
                addIfComma(toRemove, prev);
            }
            for (PsiElement psiElement : toRemove) {
                psiElement.delete();
            }
        }

        private boolean addIfComma(List<PsiElement> toRemove, PsiElement element) {
            if (PsiUtil.isComma(element)) {
                toRemove.add(element);
                return true;
            } else {
                return false;
            }
        }
    }

    public static class AddInheritedAction extends PascalBaseFix {

        @Nls
        @NotNull
        public String getName() {
            return message("action.inherited.add");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement end = descriptor.getPsiElement();
            PsiElement code = end != null ? end.getParent() : null;
            if (code != null) {
                addElements(code, end, false, "inherited", ";");
            }
        }
    }

    public static class AddResultAssignmentAction extends PascalBaseFix {

        @Nls
        @NotNull
        public String getName() {
            return message("action.result.assignment.add");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            if ((null == element) || (element.getParent() == null)) {
                return;
            }
            PsiElement parent = element.getParent();
            if (element instanceof PasExitStatement) {
                addElements(parent, element, false, "Result", ":=", "0", ";");
            } else {
                PsiElement begin = parent.getFirstChild();
                if (begin != null) {
                    addElements(parent, begin, true, "Result", ":=", "0", ";");
                }
            }
        }

    }

    private static void addElements(PsiElement parent, PsiElement anchor, boolean after, String first, String...other) {
        PsiElement newElement;
        Project project = parent.getProject();
        if (after) {
            newElement = parent.addAfter(PasElementFactory.createElementFromText(project, first), anchor);
        } else {
            newElement = parent.addBefore(PasElementFactory.createElementFromText(project, first), anchor);
        }
        for (String elementText : other) {
            newElement = parent.addAfter(PasElementFactory.createElementFromText(project, elementText), newElement);
        }
        PasModule module = PsiUtil.getElementPasModule(parent);
        if (module != null) {
            DocUtil.reformatRange(module, parent.getTextRange().getStartOffset(), parent.getTextRange().getEndOffset());
        }
    }

}
