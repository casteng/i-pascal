package com.siberika.idea.pascal.ide.intention;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.ide.actions.quickfix.IdentQuickFixes;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.PasBlockBody;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasProcBodyBlock;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

class CreateFieldForParamAction extends BaseElementAtCaretIntentionAction {

    PascalNamedElement namedElement;

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.create.field", namedElement != null ? namedElement.getName() : "");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "Parameter/" + getClass().getSimpleName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!PsiUtil.isElementUsable(element)) {
            return false;
        }
        if (element.getNode().getElementType() == PasTypes.NAME) {
            element = element.getParent();
        }
        if (element instanceof PasNamedIdent && element.getParent() instanceof PasFormalParameter) {
            namedElement = (PascalNamedElement) element;
            PascalRoutine routine = getRoutine(namedElement);
            if (routine != null) {
                PasEntityScope scope = routine.getContainingScope();
                return (scope instanceof PascalStructType) && (scope.getField(getFieldName(namedElement.getName())) == null);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        if ((element instanceof PasNamedIdent) && (element.getParent() instanceof PasFormalParameter)) {
            namedElement = (PascalNamedElement) element;
        }
        PascalRoutine routine = getRoutine(namedElement);
        if (routine != null) {
            PasEntityScope classScope = routine.getContainingScope();
            if (classScope instanceof PascalStructType) {
                String fieldName = getFieldName(namedElement.getName());
                PascalActionDeclare.ActionCreateField cfa = new PascalActionDeclare.ActionCreateField(getText(), RoutineUtil.getParameterType(namedElement), namedElement, classScope) {
                    public void afterExecution(Editor editor, PsiFile file, TemplateState state) {
                        addParamAssignment(classScope, routine, namedElement, fieldName);
                    }
                };
                cfa.invoke(project, editor, routine.getContainingFile());
            }
        }
    }

    static String getFieldName(String name) {
        return "F" + getPropertyName(name);
    }

    static PascalRoutine getRoutine(PascalNamedElement element) {
        if (PsiUtil.isFormalParameterName(element) && (element.getParent().getParent() instanceof PasFormalParameterSection)
                && (element.getParent().getParent().getParent() instanceof PascalRoutine)) {
            return (PascalRoutine) element.getParent().getParent().getParent();
        }
        return null;
    }

    static void addParamAssignment(PasEntityScope classScope, PascalRoutine routine, PascalNamedElement namedElement, String fieldName) {
        if (routine instanceof PascalExportedRoutine) {
            PsiElement element = SectionToggle.retrieveImplementation(routine, true);
            routine = element instanceof PasRoutineImplDecl ? (PascalRoutine) element : null;
        }
        if (routine instanceof PasRoutineImplDecl) {
            PasField fieldAdded = classScope.getField(fieldName);
            PascalNamedElement assignment = fieldAdded != null ? getAssignment(fieldAdded.getElement()) : null;
            if (assignment != null) {             // assignment already exists
                return;
            }
            boolean after = true;
            PsiElement anchor = null;
            for (String parameterName : routine.getFormalParameterNames()) {
                if (namedElement.getName().equalsIgnoreCase(parameterName)) {
                    if (anchor != null) {
                        break;
                    }
                    after = false;
                } else {
                    PasField paramField = classScope.getField(getFieldName(parameterName));
                    PsiElement newAnchor = paramField != null ? getAssignment(paramField.getElement()) : null;
                    if (newAnchor != null) {
                        anchor = PsiUtil.skipToExpressionParent(newAnchor);  // should be immediate child of routine statement block
                        anchor = ((anchor != null) && after) ? anchor.getNextSibling() : anchor;
                        if (!after) {
                            break;
                        }
                    }
                }
            }

            PsiElement parent = getRoutineStmtBlock(routine);
            if (null == anchor) {
                after = true;
                anchor = parent != null ? parent.getFirstChild() : null;
            }
            if ((parent != null) && (anchor != null)) {
                PsiElement anchorFinal = anchor;
                boolean afterFinal = after;
                ApplicationManager.getApplication().runWriteAction(
                        () -> IdentQuickFixes.addElements(parent, anchorFinal, afterFinal, fieldName, ":=", namedElement.getName(), ";")
                );
            }
        }
    }

    private static String getPropertyName(String name) {
        return StringUtil.capitalize(name);
    }

    private static PascalNamedElement getAssignment(PascalNamedElement element) {
        Query<PsiReference> usages = ReferencesSearch.search(element, new LocalSearchScope(element.getContainingFile()));
        AtomicReference<PascalNamedElement> result = new AtomicReference<>();
        usages.forEach(new Processor<PsiReference>() {
            @Override
            public boolean process(PsiReference psiReference) {
                PsiElement el = psiReference.getElement();
                if (el instanceof PascalNamedElement) {
                    if (ContextUtil.isAssignLeftPart((PascalNamedElement) el)) {
                        result.set((PascalNamedElement) el);
                        return false;
                    }
                }
                return true;
            }
        });
        return result.get();
    }

    private static PsiElement getRoutineStmtBlock(PascalRoutine routine) {
        PasRoutineImplDecl routineImpl = (PasRoutineImplDecl) routine;
        PasProcBodyBlock body = routineImpl.getProcBodyBlock();
        PasBlockLocal block = body != null ? body.getBlockLocal() : null;
        PasBlockBody blockBody = block != null ? block.getBlockBody() : null;
        return blockBody != null ? blockBody.getCompoundStatement() : null;
    }

}
