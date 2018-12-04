package com.siberika.idea.pascal.util;

import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.ide.DataManager;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.ide.util.PsiElementModuleRenderer;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.awt.RelativePoint;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 21/07/2015
 */
public class EditorUtil {

    public static final int NO_ITEMS_HINT_TIMEOUT_MS = 2000;

    public static <T extends PsiElement> void navigateTo(Editor editor, String title, Collection<T> targets) {
        PsiElementListNavigator.openTargets(editor, targets.toArray(new NavigatablePsiElement[targets.size()]),
                title, null, new MyPsiElementCellRenderer());
    }

    public static <T extends PsiElement> void navigateTo(MouseEvent event, String title, @Nullable String emptyTitle, Collection<T> targets) {
        if (!targets.isEmpty()) {
            PsiElementListNavigator.openTargets(event, targets.toArray(new NavigatablePsiElement[targets.size()]),
                    title, null, new MyPsiElementCellRenderer());
        } else if (!StringUtils.isEmpty(emptyTitle)) {
            showErrorHint(emptyTitle, new RelativePoint(event));
        }
    }

    public static void showErrorHint(String title, RelativePoint relativePoint) {
        final JLabel label = new JLabel(title);
        label.setBorder(HintUtil.createHintBorder());
        label.setBackground(HintUtil.getErrorColor());
        label.setOpaque(true);
        HintManager.getInstance().showHint(label, relativePoint, 0, NO_ITEMS_HINT_TIMEOUT_MS);
    }

    public static void showInformationHint(Editor editor, String message) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                HintManager.getInstance().showInformationHint(editor, message);
            }
        });
    }

    public static RelativePoint getHintPos(Editor editor) {
        return new RelativePoint(editor.getComponent(), new Point(0, 0));
    }

    public static void moveToLineEnd(Editor editor) {
        EditorActionHandler actionHandler = EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_END);
        final DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
        if (dataContext != null) {
            actionHandler.execute(editor, editor.getCaretModel().getCurrentCaret(), dataContext);
        }
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
                StringBuilder sb = new StringBuilder();
                if (element instanceof PasEntityScope) {
                    PasEntityScope owner = ((PasEntityScope) element).getContainingScope();
                    if (owner != null) {
                        sb.append(owner.getName()).append(".");
                    }
                }
                sb.append(ResolveUtil.cleanupName(PsiUtil.getFieldName((PascalNamedElement) element)));
                return sb.toString();
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
        if (file != null) {
            String line;
            if (!PsiUtil.isFromLibrary(element)) {
                Document doc = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
                line = (doc != null) ? String.valueOf(doc.getLineNumber(element.getTextOffset()) + 1) : "-";
            } else {
                line = "-";
            }
            return String.format("%s (%s)", file.getName(), line);
        }
        return "-";
    }
}
