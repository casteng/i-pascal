package com.siberika.idea.pascal.lang.inspection;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.editor.PascalRoutineActions;
import com.siberika.idea.pascal.ide.actions.quickfix.PascalBaseFix;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalInterfaceDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.search.PascalDefinitionsSearch;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.siberika.idea.pascal.PascalBundle.message;

@SuppressWarnings("Convert2Lambda")
public class NotImplementedInspection extends PascalLocalInspectionBase {

    @Override
    protected void checkClass(PasClassTypeDecl classTypeDecl, ProblemsHolder holder, boolean isOnTheFly) {
        PasClassParent classParent = classTypeDecl.getClassParent();
        if ((null != classParent) && hasNoDescendants(classTypeDecl)) {
            SmartList<PasExportedRoutine> notImplemented = new SmartList<>();
            processClass(notImplemented, new SmartHashSet<>(), classTypeDecl);
            Set<String> processed = new SmartHashSet<>();
            for (PasExportedRoutine routine : notImplemented) {
                String name = routine.getCanonicalName();
                if (!processed.contains(name)) {
                    processed.add(name);
                    PasEntityScope scope = routine.getContainingScope();
                    String scopeName = scope != null ? ResolveUtil.cleanupName(scope.getName()) + "." : "";
                    name = scopeName + name;
                    holder.registerProblem(holder.getManager().createProblemDescriptor(classParent, message("inspection.warn.methods.not.implemented", name),
                            true, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly,
                            new ImplementMethodFix(routine)));
                }
            }
        }
    }

    private void processClass(List<PasExportedRoutine> result, SmartHashSet<PascalStructType> processed, @NotNull PasClassTypeDecl classTypeDecl) {
        processed.add(classTypeDecl);
        List<SmartPsiElementPointer<PasEntityScope>> parentScope = classTypeDecl.getParentScope();
        for (SmartPsiElementPointer<PasEntityScope> parentPtr : parentScope) {
            PasEntityScope el = parentPtr.getElement();
            if (el instanceof PascalInterfaceDecl) {
                processInterface(result, processed, classTypeDecl, (PascalInterfaceDecl) el);
            }
        }

        Iterator<SmartPsiElementPointer<PasEntityScope>> parentIter = parentScope.iterator();
        if (parentIter.hasNext()) {
            PasEntityScope el = parentIter.next().getElement();
            if (el instanceof PasClassTypeDecl && !processed.contains(el)) {
                processClass(result, processed, (PasClassTypeDecl) el);
            }
        }
        for (Iterator<PasExportedRoutine> iterator = result.iterator(); iterator.hasNext(); ) {
            PasExportedRoutine method = iterator.next();
            if (isMethodImplemented(classTypeDecl, method)) {
                iterator.remove();
            }
        }
    }

    private void processInterface(List<PasExportedRoutine> result, SmartHashSet<PascalStructType> processed, @NotNull PasClassTypeDecl classDecl, PascalInterfaceDecl interfaceDecl) {
        for (PasExportedRoutine method : interfaceDecl.getMethods()) {
            if (isMethodImplemented(classDecl, method)) {
            } else {
                result.add(method);
            }
        }
        processed.add(interfaceDecl);
        for (SmartPsiElementPointer<PasEntityScope> parent : interfaceDecl.getParentScope()) {
            PasEntityScope el = parent.getElement();
            if (el instanceof PascalInterfaceDecl && !processed.contains(el)) {
                processInterface(result, processed, classDecl, (PascalInterfaceDecl) el);
            }
        }
    }

    private boolean isMethodImplemented(PasClassTypeDecl classTypeDecl, PasExportedRoutine method) {
        return classTypeDecl.getRoutine(method.getReducedName()) != null;
    }

    private boolean hasNoDescendants(PasClassTypeDecl classTypeDecl) {
        return PascalDefinitionsSearch.processDescendingStructs(classTypeDecl, false, new Processor<PasEntityScope>() {
            @Override
            public boolean process(PasEntityScope pascalStructType) {
                return false;
            }
        });
    }

    private class ImplementMethodFix extends PascalBaseFix {
        private final SmartPsiElementPointer<PasExportedRoutine> routinePtr;

        ImplementMethodFix(PasExportedRoutine routine) {
            this.routinePtr = PsiUtil.createSmartPointer(routine);
        }

        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getName() {
            PasExportedRoutine routine = routinePtr != null ? routinePtr.getElement() : null;
            return message("action.implement.method", routine != null ? routine.getCanonicalName() : "?");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PasExportedRoutine routine = routinePtr != null ? routinePtr.getElement() : null;
            PasEntityScope scope = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PasEntityScope.class);
            if ((null == routine) || (null == scope)) {
                return;
            }
            final int offs = RoutineUtil.calcMethodPos(scope, null);
            Editor editor = EditorUtil.getEditor(project);
            if (offs < 0) {
                EditorUtil.showErrorHint(PascalBundle.message("action.error.find.position"), EditorUtil.getHintPos(editor));
                return;
            }
            final Document document = DocUtil.getDocument(scope);
            if (document != null) {
                CommandProcessor.getInstance().setCurrentCommandName(getName());
                CharSequence text = RoutineUtil.prepareRoutineHeaderText(routine.getText(), "", "override") + "\n";
                document.insertString(offs, text);

                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                PsiDocumentManager.getInstance(project).commitDocument(document);
                DocUtil.reformat(scope.getParent(), true);
                PasField addedRoutineField = scope.getField(PsiUtil.getFieldName(routine));
                PascalNamedElement addedRoutine = addedRoutineField != null ? addedRoutineField.getElement() : null;
                if (PsiUtil.isElementUsable(addedRoutine)) {
                    PsiFile file = scope.getContainingFile();
                    PascalRoutineActions.ActionImplement act = new PascalRoutineActions.ActionImplement(message("action.implement"), addedRoutine);
                    act.invoke(project, editor, file);
                }
            }
        }
    }

}
