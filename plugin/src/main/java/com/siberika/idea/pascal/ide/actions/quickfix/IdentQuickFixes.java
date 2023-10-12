package com.siberika.idea.pascal.ide.actions.quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.PascalDocumentationProvider;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExitStatement;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalVariableDeclaration;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.lang.references.resolve.Types;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StmtUtil;
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
            if (parent instanceof PascalVariableDeclaration) {
                toRemove.add(identName);
                if (((PascalVariableDeclaration) parent).getNamedIdentDeclList().size() <= 1) {
                    toRemove.add(parent);
                    PsiElement section = parent.getParent();
                    if (section instanceof PasVarSection) {
                        if (((PasVarSection) section).getVarDeclarationList().size() <= 1) {
                            toRemove.add(section);
                        }
                    } else if (section instanceof PasFormalParameterSection) {
                        List<PasFormalParameter> params = ((PasFormalParameterSection) section).getFormalParameterList();
                        if (!params.isEmpty() && (params.get(0) == parent)) {       // First
                            addSeparatorsToRemove(toRemove, parent, true);
                        } else if (params.size() > 1) {
                            toRemove.remove(parent);                       // parameter should be removed after preceding separators
                            addSeparatorsToRemove(toRemove, parent, false);
                            toRemove.add(parent);
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

        private final static TokenSet WS_SEMI = TokenSet.create(PasTypes.SEMI, TokenType.WHITE_SPACE, PasTypes.COMMENT);

        private void addSeparatorsToRemove(List<PsiElement> toRemove, PsiElement param, boolean forward) {
            PsiElement el = forward ? param.getNextSibling() : param.getPrevSibling();
            while (WS_SEMI.contains(el.getNode().getElementType())) {
                if (el.getNode().getElementType() != PasTypes.COMMENT) {
                    toRemove.add(el);
                }
                el = forward ? el.getNextSibling() : el.getPrevSibling();
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
            PasEntityScope scope = PsiUtil.getNearestAffectingScope(element);
            PasField.ValueType type = null;
            if (scope instanceof PascalRoutine) {
                PasEntityScope parent = scope.getContainingScope();
                if (parent != null) {
                    PasField field = parent.getField(((PascalRoutine) scope).getCanonicalName());
                    if (null == field) {
                        field = parent.getField(RoutineUtil.getCanonicalNameWoScope((PascalRoutine) scope));
                    }
                    type = field != null ? field.getValueType(0) : null;
                }
            }
            String typeDefault = Types.getTypeDefaultValueStr(type);
            PsiElement parent = element.getParent();
            if (element instanceof PasExitStatement) {
                element = StmtUtil.findAssignmentLocation(element.getParent());
                addElements(element.getParent(), element, false, "Result", ":=", typeDefault, ";");
            } else {
                PsiElement begin = parent.getFirstChild();
                if (begin != null) {
                    addElements(parent, begin, true, "Result", ":=", typeDefault, ";");
                }
            }
        }

    }

    public static void addElements(PsiElement parent, PsiElement anchor, boolean after, String first, String... other) {
        PsiElement newElement;
        Project project = parent.getProject();
        if (after) {
            newElement = parent.addAfter(PasElementFactory.createElementFromText(project, first), anchor);
        } else {
            newElement = parent.addBefore(PasElementFactory.createElementFromText(project, first), anchor);
        }
        for (String elementText : other) {
            if ((elementText != null) && (elementText.length() > 0)) {
                newElement = parent.addAfter(PasElementFactory.createElementFromText(project, elementText), newElement);
            }
        }
        PasModule module = PsiUtil.getElementPasModule(parent);
        if (module != null) {
            DocUtil.reformatRange(module, parent.getTextRange().getStartOffset(), parent.getTextRange().getEndOffset());
        }
    }

}
