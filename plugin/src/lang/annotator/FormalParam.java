package com.siberika.idea.pascal.lang.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
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
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class FormalParam {
    public static void annotate(PsiElement element, AnnotationHolder holder) {
        if (element instanceof PasNamedIdent && element.getParent() instanceof PasFormalParameter) {
            PascalNamedElement namedElement = (PascalNamedElement) element;
            PascalRoutine routine = geRoutine(namedElement);
            if (routine != null) {
                PasEntityScope scope = routine.getContainingScope();
                if (scope instanceof PascalStructType) {
                    String name = getFieldName(namedElement.getName());
                    if (scope.getField(name) == null) {
                        Annotation ann = holder.createInfoAnnotation(element, PascalBundle.message("action.fix.create.field", namedElement.getName()));
                        ann.registerFix(new CreateFieldFix(namedElement));
                        ann.registerFix(new CreatePropertyFix(namedElement));
                    }
                }
            }
        }
    }

    private static String getPropertyName(String name) {
        return StringUtil.capitalize(name);
    }

    private static String getFieldName(String name) {
        return "F" + getPropertyName(name);
    }

    private static PascalRoutine geRoutine(PascalNamedElement element) {
        if (PsiUtil.isFormalParameterName(element) && (element.getParent().getParent() instanceof PasFormalParameterSection)
                && (element.getParent().getParent().getParent() instanceof PascalRoutine)) {
            return (PascalRoutine) element.getParent().getParent().getParent();
        }
        return null;
    }

    private abstract static class CreateFromParamFixBase extends FixBase<PascalNamedElement> {

        CreateFromParamFixBase(PascalNamedElement element) {
            super(element);
        }

        void addParamAssignment(PasEntityScope classScope, PascalRoutine routine, PascalNamedElement namedElement, String fieldName) {
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
                        PasField paramField = classScope.getField("F" + getPropertyName(parameterName));
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

        private PascalNamedElement getAssignment(PascalNamedElement element) {
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
    }



    private static PsiElement getRoutineStmtBlock(PascalRoutine routine) {
        PasRoutineImplDecl routineImpl = (PasRoutineImplDecl) routine;
        PasProcBodyBlock body = routineImpl.getProcBodyBlock();
        PasBlockLocal block = body != null ? body.getBlockLocal() : null;
        PasBlockBody blockBody = block != null ? block.getBlockBody() : null;
        return blockBody != null ? blockBody.getCompoundStatement() : null;
    }

    private static class CreateFieldFix extends CreateFromParamFixBase {
        CreateFieldFix(PascalNamedElement param) {
            super(param);
        }

        @NotNull
        @Override
        public String getText() {
            return PascalBundle.message("action.fix.create.field", element.getName());
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            PascalRoutine routine = geRoutine(element);
            if (routine != null) {
                PasEntityScope classScope = routine.getContainingScope();
                if (classScope instanceof PascalStructType) {
                    String fieldName = getFieldName(element.getName());
                    PascalActionDeclare.ActionCreateField cfa = new PascalActionDeclare.ActionCreateField(getText(), RoutineUtil.getParameterType(element), element, classScope) {
                        public void afterExecution(Editor editor, PsiFile file) {
                            addParamAssignment(classScope, routine, element, fieldName);
                        }
                    };
                    cfa.invoke(project, editor, routine.getContainingFile());
                }
            }
        }
    }

    private static class CreatePropertyFix extends CreateFromParamFixBase {
        CreatePropertyFix(PascalNamedElement param) {
            super(param);
        }

        @NotNull
        @Override
        public String getText() {
            return PascalBundle.message("action.fix.create.property", element.getName());
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            PascalRoutine routine = geRoutine(element);
            if (routine != null) {
                PasEntityScope classScope = routine.getContainingScope();
                if (classScope instanceof PascalStructType) {
                    String fieldName = getFieldName(element.getName());
                    PascalActionDeclare.ActionCreatePropertyHP cfa = new PascalActionDeclare.ActionCreatePropertyHP(getText(), element, RoutineUtil.getParameterType(element), classScope) {
                        public void afterExecution(Editor editor, PsiFile file) {
                            addParamAssignment(classScope, routine, element, fieldName);
                        }
                    };
                    cfa.invoke(project, editor, routine.getContainingFile());
                }
            }
        }
    }

}
