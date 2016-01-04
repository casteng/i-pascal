package com.siberika.idea.pascal.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.editor.PascalRoutineActions;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.ui.TreeViewStruct;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.Filter;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.siberika.idea.pascal.PascalBundle.message;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2015
 */
public class ActionImplement extends PascalAction {
    public void actionPerformed(AnActionEvent e) {
        PsiElement el = getElement(e);
        PascalRoutineImpl methodImpl = null;
        PasEntityScope scope = PsiTreeUtil.getParentOfType(el, PasEntityScope.class);
        if (scope instanceof PascalRoutineImpl) {
            methodImpl = (PascalRoutineImpl) scope;
            scope = scope.getContainingScope();
        }
        if (!(scope instanceof PascalStructType)) {
            EditorUtil.showErrorHint(PascalBundle.message("action.error.notinstruct"), EditorUtil.getHintPos(getEditor(e)));
            return;
        }
        Collection<PasEntityScope> structs = new SmartList<PasEntityScope>();
        GotoSuper.getParentStructs(structs, scope);
        final Set<String> existing = new HashSet<String>();
        for (PasField field : scope.getAllFields()) {
            allowNonExistingRoutines(field, existing);
        }

        TreeViewStruct tree = new TreeViewStruct(el.getProject(), PascalBundle.message("title.override.methods", scope.getName()), structs, new Filter<PasField>() {
            @Override
            public boolean allow(PasField value) {
                return allowNonExistingRoutines(value, existing);
            }
        });
        tree.show();

        doOverride(getEditor(e), scope, el, methodImpl, tree.getSelected());
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
    private void doOverride(final Editor editor, final PasEntityScope scope, final PsiElement el, PascalRoutineImpl methodImpl, final List<PasField> selected) {
        PsiElement prevMethod = getPrevMethod(el, methodImpl);
        final AtomicInteger offs = new AtomicInteger();
        if (prevMethod != null) {
            offs.set(prevMethod.getTextRange().getEndOffset());
        } else {
            PsiElement pos = PsiUtil.findEndSibling(scope.getFirstChild());
            offs.set(pos != null ? pos.getTextRange().getStartOffset() : -1);
        }
        if (offs.get() < 0) {
            EditorUtil.showErrorHint(PascalBundle.message("action.error.find.position"), EditorUtil.getHintPos(editor));
            return;
        }

        final Document document = editor.getDocument();
        for (final PasField field : selected) {
            new WriteCommandAction(el.getProject()) {
                @Override
                protected void run(@NotNull Result result) throws Throwable {
                    CommandProcessor.getInstance().setCurrentCommandName(PascalBundle.message("action.override"));
                    PascalNamedElement element = field.getElement();
                    if (PsiUtil.isElementUsable(element)) {
                        CharSequence text = prepareText(element.getText());
                        document.insertString(offs.get(), text);
                        offs.addAndGet(text.length());
                    }
                    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                    PsiDocumentManager.getInstance(el.getProject()).commitDocument(document);
                }
            }.execute();
        }
        DocUtil.reformat(scope, true);
        for (final PasField field : selected) {
            PascalNamedElement element = field.getElement();
            if (PsiUtil.isElementUsable(element)) {
                PasField routine = scope.getField(PsiUtil.getFieldName(field.getElement()));
                PascalRoutineActions.ActionImplement act = routine != null ? new PascalRoutineActions.ActionImplement(message("action.implement"), routine.getElement()) : null;
                if (act != null) {
                    act.invoke(el.getProject(), editor, el.getContainingFile());
                }
            }
        }
    }

    private CharSequence prepareText(String text) {
        return text.replace("virtual", "override").replaceAll("abstract\\s*;", "");
    }

    private PsiElement getPrevMethod(PsiElement el, PascalRoutineImpl methodImpl) {
        if (methodImpl != null) {
            return SectionToggle.retrieveDeclaration(methodImpl);
        }
        PasExportedRoutine routine = (el instanceof PasExportedRoutine) ? (PasExportedRoutine) el : PsiTreeUtil.getParentOfType(el, PasExportedRoutine.class);
        if (null == routine) {
            routine = PsiTreeUtil.getPrevSiblingOfType(el, PasExportedRoutine.class);
        }
        return routine;
    }

}
