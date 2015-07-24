package com.siberika.idea.pascal.util;

import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.ide.util.PsiElementModuleRenderer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 21/07/2015
 */
public class EditorUtil {
    public static <T extends PsiElement> void navigateTo(Editor editor, String title, Collection<T> targets) {
        PsiElementListNavigator.openTargets(editor, targets.toArray(new NavigatablePsiElement[targets.size()]),
                title, null, new MyPsiElementCellRenderer());
    }

    public static <T extends PsiElement> void navigateTo(MouseEvent event, String title, Collection<T> targets) {
        PsiElementListNavigator.openTargets(event, targets.toArray(new NavigatablePsiElement[targets.size()]),
                title, null, new MyPsiElementCellRenderer());
    }

    private static class MyPsiElementCellRenderer extends DefaultPsiElementCellRenderer {

        @Nullable
        @Override
        protected DefaultListCellRenderer getRightCellRenderer(final Object value) {
            return new PsiElementModuleRenderer() {
                @Override
                public String getText() {
                    if (value instanceof PsiElement) {
                        return getRightText((PsiElement) value);
                    } else {
                        return super.getText();
                    }
                }
            };
        }

        @Override
        public String getElementText(PsiElement element) {
            if (element instanceof PascalNamedElement) {
                String ownerName = "";
                if (element instanceof PasEntityScope) {
                    PasEntityScope owner = ((PasEntityScope) element).getContainingScope();
                    ownerName = owner != null ? owner.getName() : ownerName;
                }
                return String.format("%s.%s", ownerName, PsiUtil.getFieldName((PascalNamedElement) element));
            } else {
                return element.getText();
            }
        }

        @Override
        public String getContainerText(PsiElement element, String name) {
            return "-";
        }
    }

    private static String getRightText(PsiElement element) {
        PsiFile file = element.getContainingFile();
        Document doc = file != null ? PsiDocumentManager.getInstance(element.getProject()).getDocument(file) : null;
        if (doc != null) {
            int line = doc.getLineNumber(element.getTextOffset()) + 1;
            return String.format("%s (%d)", file.getName(), line);
        }
        return "-";
    }
}
