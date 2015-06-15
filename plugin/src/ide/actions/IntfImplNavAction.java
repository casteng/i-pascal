package com.siberika.idea.pascal.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;

/**
 * Author: George Bakhtadze
 * Date: 28/05/2015
 */
public class IntfImplNavAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if ((null == file) || (null == editor)) {
            return;
        }
        PsiElement target = null;
        PsiElement el = file.findElementAt(editor.getCaretModel().getOffset());
        target = getRoutineTarget(PsiTreeUtil.getParentOfType(el, PascalRoutineImpl.class));
        if (null == target) {
            target = getUsesTarget(PsiTreeUtil.getParentOfType(el, PasUsesClause.class));
        }
        navigateTo(editor, target);
    }

    private PsiElement getUsesTarget(PasUsesClause usesClause) {
        if (usesClause != null) {
            PsiElement impl = PsiUtil.getModuleImplementationSection(usesClause.getContainingFile());
            PsiElement intf = PsiUtil.getModuleInterfaceSection(usesClause.getContainingFile());
            if ((impl != null) && (intf != null)) {
                if (PsiUtil.isParentOf(usesClause, impl)) {
                    return PsiTreeUtil.findChildOfType(intf, PasUsesClause.class);
                } else if (PsiUtil.isParentOf(usesClause, intf)) {
                    return PsiTreeUtil.findChildOfType(impl, PasUsesClause.class);
                }
            }
        }
        return null;
    }

    private PsiElement getRoutineTarget(PascalRoutineImpl routine) {
        Container cont = getPrefix(new Container(routine));
        if (routine instanceof PasExportedRoutine) {
            return retrieveImplementations(cont);
        } else if (routine instanceof PasRoutineImplDecl) {
            return retrieveDeclarations(cont);
        }
        return null;
    }

    private void navigateTo(Editor editor, PsiElement target) {
        if (target != null) {
            editor.getCaretModel().moveToOffset(target.getTextOffset());
            editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
        }
    }

    private PsiElement retrieveImplementations(Container container) {
        if (null == container) {
            return null;
        }
        PasField field = null;
        if (container.scope instanceof PasModuleImpl) {
            field = ((PasModuleImpl) container.scope).getPrivateField(container.prefix + PsiUtil.getFieldName(container.routine));
        }
        return field != null ? field.element : null;
    }

    private PsiElement retrieveDeclarations(Container container) {
        if (null == container) {
            return null;
        }
        PasField field = null;
        PasEntityScope scope = container.routine.getContainingScope();
        if (scope != null) {
            String ns = container.routine.getNamespace();
            field = scope.getField(PsiUtil.getFieldName(container.routine).substring(StringUtils.isEmpty(ns) ? 0 : ns.length()+1));
        }
        return field != null ? field.element : null;
    }

    private Container getPrefix(Container current) {
        while ((current.scope != null) && !(current.scope instanceof PascalModuleImpl)) {
            current.scope = findOwner(current.scope);
            if (current.scope instanceof PascalStructType) {
                current.prefix = current.scope.getName() + "." + current.prefix;
            } else if (current.scope instanceof PascalRoutineImpl) {
                current.routine = (PascalRoutineImpl) current.scope;
            }
        }
        return current;
    }

    private static PasEntityScope findOwner(PasEntityScope scope) {
        return scope.getContainingScope();
    }

    private static class Container {
        String prefix = "";
        PascalRoutineImpl routine;
        PasEntityScope scope;

        public Container(PascalRoutineImpl routine) {
            this.routine = routine;
            this.scope = routine;
        }
    }
}
