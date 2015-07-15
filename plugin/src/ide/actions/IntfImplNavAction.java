package com.siberika.idea.pascal.ide.actions;

import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Author: George Bakhtadze
 * Date: 28/05/2015
 */
public class IntfImplNavAction extends AnAction {

    public static final Logger LOG = Logger.getInstance(IntfImplNavAction.class.getName());
    private static final int MAX_RECURSION = 10;

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if ((null == file) || (null == editor)) {
            return;
        }
        PsiElement target;
        PsiElement el = file.findElementAt(editor.getCaretModel().getOffset());
        target = getRoutineTarget(PsiTreeUtil.getParentOfType(el, PascalRoutineImpl.class));
        if (null == target) {
            target = getUsesTarget(PsiTreeUtil.getParentOfType(el, PasUsesClause.class));
        }
        Collection<PsiElement> targets;
        if (PsiUtil.isElementUsable(target)) {
            targets = Collections.singletonList(target);
        } else {
            targets = new SmartList<PsiElement>();
            getStructTarget(targets, el);
        }
        if (!targets.isEmpty()) {
            navigateTo(editor, targets);
        }
    }

    private void getStructTarget(Collection<PsiElement> targets, PsiElement element) {
        PascalStructType struct = getStructByElement(element);
        if (struct != null) {
            Container cont = calcPrefix(new Container(struct));
            findDescendingStructs(targets, struct, 0);
            //return Collections.singletonList(retrieveFirstImplementations(cont));
        }
    }

    private PascalStructType getStructByElement(PsiElement element) {
        PascalStructType struct = PsiUtil.getStructTypeByName(PsiTreeUtil.getParentOfType(element, PascalNamedElement.class));
        return struct != null ? struct : PsiTreeUtil.getParentOfType(element, PascalStructType.class);
    }

    private void findDescendingStructs(Collection<PsiElement> targets, PascalStructType struct, int rCnt) {
        if (rCnt > MAX_RECURSION) {
            LOG.error("Max recursion reached");
            return;
        }
        if (null == struct.getNameIdentifier()) {
            return;
        }
        for (PsiReference psiReference : ReferencesSearch.search(struct.getNameIdentifier())) {
            if (PsiUtil.isClassParent(psiReference.getElement())) {
                struct = getStructByElement(psiReference.getElement());
                if (PsiUtil.isElementUsable(struct)) {
                    targets.add(struct);
                    findDescendingStructs(targets, struct, rCnt + 1);
                }
            }
        }
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
        Container cont = calcPrefix(new Container(routine));
        if (routine instanceof PasExportedRoutine) {
            return retrieveImplementation(cont);
        } else if (routine instanceof PasRoutineImplDecl) {
            return retrieveDeclaration(cont);
        }
        return null;
    }

    private void navigateTo(Editor editor, Collection<PsiElement> targets) {
        PsiElementListNavigator.openTargets(editor, targets.toArray(new NavigatablePsiElement[targets.size()]),
                PascalBundle.message("navigate.to.title"), null, new DefaultPsiElementCellRenderer());
    }

    private PsiElement retrieveImplementation(Container container) {
        if (null == container) {
            return null;
        }
        PasField field = null;
        if (container.scope instanceof PasModuleImpl) {
            field = ((PasModuleImpl) container.scope).getPrivateField(container.prefix + PsiUtil.getFieldName(container.element));
        }
        return field != null ? field.element : null;
    }

    private PsiElement retrieveFirstImplementations(Container container) {
        if (null == container) {
            return null;
        }
        if (container.scope instanceof PasModuleImpl) {
            String prefix = (container.prefix + container.element.getName()).toUpperCase();
            Set<PasField> res = new TreeSet<PasField>(new Comparator<PasField>() {
                @Override
                public int compare(PasField o1, PasField o2) {
                    if ((null == o1.element) || (null == o2.element)) {
                        return 0;
                    }
                    return o1.element.getTextOffset() - o2.element.getTextOffset();
                }
            });
            for (PasField field : container.scope.getAllFields()) {
                if ((field.fieldType == PasField.FieldType.ROUTINE) && (field.name.toUpperCase().startsWith(prefix))) {
                    res.add(field);
                }
            }
            if (!res.isEmpty()) {
                return res.iterator().next().element;
            }
        }
        return null;
    }

    private PsiElement retrieveDeclaration(Container container) {
        if (null == container) {
            return null;
        }
        PasField field = null;
        PasEntityScope scope = container.element.getContainingScope();
        if (scope != null) {
            String ns = container.element.getNamespace();
            field = scope.getField(PsiUtil.getFieldName(container.element).substring(StringUtils.isEmpty(ns) ? 0 : ns.length()+1));
        }
        return field != null ? field.element : null;
    }

    private Container calcPrefix(Container current) {
        while ((current.scope != null) && !(current.scope instanceof PascalModuleImpl)) {
            current.scope = findOwner(current.scope);
            if (current.scope instanceof PascalStructType) {
                current.prefix = current.scope.getName() + "." + current.prefix;
            } else if (current.scope instanceof PascalRoutineImpl) {
                current.element = current.scope;
            }
        }
        return current;
    }

    private static PasEntityScope findOwner(PasEntityScope scope) {
        return scope.getContainingScope();
    }

    private static class Container {
        String prefix = "";
        PasEntityScope element;
        PasEntityScope scope;

        public Container(PasEntityScope element) {
            this.element = element;
            this.scope = element;
        }
    }
}
