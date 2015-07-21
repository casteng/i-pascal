package com.siberika.idea.pascal.util;

import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.PascalBundle;

import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 21/07/2015
 */
public class EditorUtil {
    public static void navigateTo(Editor editor, Collection<PsiElement> targets) {
        PsiElementListNavigator.openTargets(editor, targets.toArray(new NavigatablePsiElement[targets.size()]),
                PascalBundle.message("navigate.to.title"), null, new DefaultPsiElementCellRenderer());
    }
}
