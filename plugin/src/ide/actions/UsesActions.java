package com.siberika.idea.pascal.ide.actions;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.PascalImportOptimizer;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.stub.PascalUnitSymbolIndex;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.openapi.actionSystem.ActionPlaces.EDITOR_POPUP;

/**
 * Author: George Bakhtadze
 * Date: 21/12/2015
 */
public class UsesActions {

    public static class AddUnitAction extends BaseUsesAction {
        private final String unitName;
        private final boolean toInterface;

        public AddUnitAction(String name, String unitName, boolean toInterface) {
            super(name);
            this.unitName = unitName;
            this.toInterface = toInterface;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            PascalImportOptimizer.addUnitToSection(PsiUtil.getElementPasModule(file), Collections.singletonList(unitName), toInterface);
        }

    }

    public static class NewUnitAction extends BaseUsesAction {
        private final String unitName;

        public NewUnitAction(String name, String unitName) {
            super(name);
            this.unitName = unitName;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            final DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
            final CreateModuleAction act = new CreateModuleAction();
            final AnActionEvent ev = AnActionEvent.createFromAnAction(act, null, EDITOR_POPUP, dataContext);
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    act.actionPerformed(ev);
                }
            });
        }

    }

    public static class SearchUnitAction extends BaseUsesAction {
        private final String searchFor;
        private final boolean toInterface;

        public SearchUnitAction(String name, String searchFor, boolean toInterface) {
            super(name);
            this.searchFor = searchFor;
            this.toInterface = toInterface;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            final GlobalSearchScope scope = ProjectScope.getAllScope(project);
            final AtomicBoolean found = new AtomicBoolean();
            String searchForUpper = searchFor.toUpperCase();
            StubIndex.getInstance().processElements(PascalUnitSymbolIndex.KEY, searchForUpper, project, scope,
                    PascalNamedElement.class, new Processor<PascalNamedElement>() {
                        @Override
                        public boolean process(PascalNamedElement element) {
                            String name = element.getName();
                            if ((element instanceof PascalStubElement) &&
                                    (searchFor.equalsIgnoreCase(element.getName())
                                  || (name.toUpperCase().startsWith(searchForUpper) && (element instanceof PascalRoutine)))) {
                                String unitName = ((PascalStubElement) element).getContainingUnitName();
                                if (unitName != null) {
                                    PasEntityScope affScope = PsiUtil.getNearestAffectingScope(element);
                                    if ((affScope instanceof PasModule) && (((PasModule) affScope).getModuleType() == PascalModule.ModuleType.UNIT)) {
                                        PascalImportOptimizer.addUnitToSection(PsiUtil.getElementPasModule(file), Collections.singletonList(unitName), toInterface);
                                        found.set(true);
                                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                HintManager.getInstance().showInformationHint(editor, PascalBundle.message("action.unit.search.added", unitName));
                                            }
                                        });
                                        return false;
                                    }
                                }
                            }
                            return true;
                        }
                    });
            if (!found.get()) {
                EditorUtil.showErrorHint(PascalBundle.message("action.unit.search.notfound", searchFor), EditorUtil.getHintPos(editor));
            }
        }
    }

    private static abstract class BaseUsesAction extends BaseIntentionAction {
        private final String name;

        private BaseUsesAction(String name) {
            this.name = name;
        }

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Pascal";
        }

        @Override
        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
            return (file != null) && PascalLanguage.INSTANCE.equals(file.getLanguage());
        }

        @NotNull
        @Override
        public String getText() {
            return name;
        }

    }

}
