package com.siberika.idea.pascal.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.ui.TreeViewStruct;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.Filter;
import com.siberika.idea.pascal.util.PsiUtil;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2015
 */
public class ActionImplement extends PascalAction {
    public void actionPerformed(AnActionEvent e) {
        PsiElement el = getElement(e);
        PasEntityScope scope = PsiTreeUtil.getParentOfType(el, PasEntityScope.class);
        if (!(scope instanceof PascalStructType)) {
            EditorUtil.showHint(PascalBundle.message("action.error.notinstruct"), new RelativePoint(getEditor(e).getComponent(), new Point(0, 0)));
            return;
        }
        Collection<PasEntityScope> structs = new SmartList<PasEntityScope>();
        Collection<PasField> methods = new SmartList<PasField>();
        GotoSuper.getParentStructs(structs, scope);
        for (PasEntityScope struct : structs) {
            for (PasField field : struct.getAllFields()) {
                if (field.fieldType == PasField.FieldType.ROUTINE) {
                    methods.add(field);
                }
            }
        }

        final Set<String> existing = new HashSet<String>();

        TreeViewStruct tree = new TreeViewStruct(el.getProject(), "Title", structs, new Filter<PasField>() {
            @Override
            public boolean allow(PasField value) {
                if (value.fieldType == PasField.FieldType.ROUTINE) {
                    String name = PsiUtil.getFieldName(value.getElement());
                    if (!existing.contains(name)) {
                        existing.add(name);
                        return true;
                    }
                }
                return false;
            }
        });
        tree.show();
        for (PasField field : tree.getSelected()) {
            System.out.println(field);
        }

    }
}
