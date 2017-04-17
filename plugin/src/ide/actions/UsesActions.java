package com.siberika.idea.pascal.ide.actions;

import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.PascalImportOptimizer;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.actionSystem.ActionPlaces.EDITOR_POPUP;

/**
 * Author: George Bakhtadze
 * Date: 21/12/2015
 */
public class UsesActions {

    public static class ExcludeUnitAction extends BaseUsesUnitAction {
        public ExcludeUnitAction(String name, PasNamespaceIdent usedUnitName) {
            super(name, usedUnitName);
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
            if (doc != null) {
                doc.insertString(usedUnitName.getTextRange().getStartOffset(), "{!}");
                PsiDocumentManager.getInstance(project).commitDocument(doc);
            }
        }
    }

    public static class OptimizeUsesAction extends BaseUsesAction implements LowPriorityAction {
        public OptimizeUsesAction(String name) {
            super(name);
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            PascalImportOptimizer.doProcess(file).run();
        }
    }

    public static class MoveUnitAction extends BaseUsesUnitAction {
        public MoveUnitAction(String name, PasNamespaceIdent usedUnitName) {
            super(name, usedUnitName);
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            TextRange range = getRangeToRemove();
            if (range != null) {
                final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
                if (doc != null) {
                    PascalImportOptimizer.addUnitToSection(PsiUtil.getElementPasModule(file), Collections.singletonList(usedUnitName.getName()), false);
                    doc.deleteString(range.getStartOffset(), range.getEndOffset());
                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                }
            }
        }

    }

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

    public static class RemoveUnitAction extends BaseUsesUnitAction {
        public RemoveUnitAction(String name, PasNamespaceIdent usedUnitName) {
            super(name, usedUnitName);
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            TextRange range = getRangeToRemove();
            if (range != null) {
                final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
                if (doc != null) {
                    doc.deleteString(range.getStartOffset(), range.getEndOffset());
                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                }
            }
        }
    }

    private static abstract class BaseUsesUnitAction extends BaseUsesAction {
        protected final PasNamespaceIdent usedUnitName;
        public BaseUsesUnitAction(String name, PasNamespaceIdent usedUnitName) {
            super(name);
            this.usedUnitName = usedUnitName;
        }

        protected TextRange getRangeToRemove() {
            PasUsesClause usesClause = (PasUsesClause) usedUnitName.getParent();
            List<TextRange> ranges = PascalImportOptimizer.getUnitRanges(usesClause);
            TextRange res = PascalImportOptimizer.removeUnitFromSection(usedUnitName, usesClause, ranges, usesClause.getNamespaceIdentList().size());
            if ((res != null) && (usesClause.getNamespaceIdentList().size() == 1)) {                                                              // Remove whole uses clause if last unit removed
                final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
                res = TextRange.create(usesClause.getTextRange().getStartOffset(), DocUtil.expandRangeEnd(doc, usesClause.getTextRange().getEndOffset(), DocUtil.RE_LF));
            }
            return res;
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
            return true;
        }

        @NotNull
        @Override
        public String getText() {
            return name;
        }

    }

}
