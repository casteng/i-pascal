package com.siberika.idea.pascal.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.PascalRoutineActions;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.RoutineUtil;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.ui.TreeViewStruct;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.Filter;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.siberika.idea.pascal.PascalBundle.message;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2015
 */
public class ActionImplement extends PascalAction {
    @Override
    public void doActionPerformed(AnActionEvent e) {
        PsiElement el = getElement(e);
        Editor editor = getEditor(e);
        showOverrideDialog(el, editor);
    }

    public void showOverrideDialog(PsiElement el, Editor editor) {
        PascalRoutine methodImpl = null;
        PasEntityScope scope = PsiTreeUtil.getParentOfType(el, PasEntityScope.class);
        if (scope instanceof PascalRoutine) {
            methodImpl = (PascalRoutine) scope;
            scope = scope.getContainingScope();
        }
        if (!(scope instanceof PascalStructType)) {
            EditorUtil.showErrorHint(message("action.error.notinstruct"), EditorUtil.getHintPos(editor));
            return;
        }
        Collection<PasEntityScope> structs = new LinkedHashSet<>(Arrays.asList(GotoSuper.searchForStruct((PascalStructType) scope).toArray(new PasEntityScope[0])));
        final Set<String> existing = new HashSet<String>();
        for (PasField field : scope.getAllFields()) {
            allowNonExistingRoutines(field, existing);
        }

        TreeViewStruct tree = new TreeViewStruct(el.getProject(), message("title.override.methods", scope.getName()), structs, new Filter<PasField>() {
            @Override
            public boolean allow(PasField value) {
                return allowNonExistingRoutines(value, existing);
            }
        });
        tree.show();

        doOverride(editor, scope, el, methodImpl, tree.getSelected());
    }

    private boolean allowNonExistingRoutines(PasField value, Set<String> existing) {
        if (value.fieldType == PasField.FieldType.ROUTINE) {
            String name = PsiUtil.getFieldName(value.getElement());
            if (!existing.contains(name)) {
                existing.add(name);
                return true;
            }
        }
        return false;
    }
    // if methodImpl = null assuming interface part

    private void doOverride(final Editor editor, final PasEntityScope scope, final PsiElement el, PascalRoutine methodImpl, final List<PasField> selected) {
        PsiElement prevMethod = getPrevMethod(el, methodImpl);
        final AtomicInteger offs = new AtomicInteger(RoutineUtil.calcMethodPos(scope, prevMethod));
        if (offs.get() < 0) {
            EditorUtil.showErrorHint(message("action.error.find.position"), EditorUtil.getHintPos(editor));
            return;
        }

        PsiFile file = el.getContainingFile();
        final Document document = editor.getDocument();
        for (final PasField field : selected) {
            WriteCommandAction.runWriteCommandAction(el.getProject(), new Runnable() {
                        @Override
                        public void run() {
                            CommandProcessor.getInstance().setCurrentCommandName(message("action.override"));
                            PascalNamedElement element = field.getElement();
                            if (PsiUtil.isElementUsable(element)) {
                                CharSequence text = RoutineUtil.prepareRoutineHeaderText(element.getText(), "override", "");
                                document.insertString(offs.get(), text);
                                offs.addAndGet(text.length());
                            }
                            editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                            PsiDocumentManager.getInstance(el.getProject()).commitDocument(document);
                        }
                    }
            );
        }
        DocUtil.reformat(scope, true);
        for (final PasField field : selected) {
            PascalNamedElement element = field.getElement();
            if (PsiUtil.isElementUsable(element)) {
                PasField routine = scope.getField(PsiUtil.getFieldName(field.getElement()));
                PascalRoutineActions.ActionImplement act = routine != null ? new PascalRoutineActions.ActionImplement(message("action.implement"), routine.getElement()) : null;
                if (act != null) {
                    act.invoke(el.getProject(), editor, file);
                }
            }
        }
    }

    private PsiElement getPrevMethod(PsiElement el, PascalRoutine methodImpl) {
        if (methodImpl != null) {
            return SectionToggle.retrieveDeclaration(methodImpl, false);
        }
        PasExportedRoutine routine = (el instanceof PasExportedRoutine) ? (PasExportedRoutine) el : PsiTreeUtil.getParentOfType(el, PasExportedRoutine.class);
        if (null == routine) {
            routine = PsiTreeUtil.getPrevSiblingOfType(el, PasExportedRoutine.class);
        }
        return routine;
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(PascalLanguage.INSTANCE.equals(e.getData(LangDataKeys.LANGUAGE)));
    }

}
