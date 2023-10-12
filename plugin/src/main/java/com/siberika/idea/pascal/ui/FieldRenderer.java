package com.siberika.idea.pascal.ui;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Author: George Bakhtadze
 * Date: 27/11/2015
 */
public class FieldRenderer extends ColoredTreeCellRenderer {

    private static final SimpleTextAttributes SMALL = new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, null);

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        PasField field = getField(value, PasField.class);
        if (null == field) {
            PasEntityScope scope = getField(value, PasEntityScope.class);
            setIcon(PascalIcons.CLASS);
            append(PsiUtil.getFieldName(scope));
            return;
        }
        setIcon(PascalIcons.ROUTINE);
        SimpleTextAttributes attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
        PascalNamedElement element = field.getElement();
        if (!PasField.isAllowed(field.visibility, PasField.Visibility.PROTECTED)) {
            attributes = SimpleTextAttributes.merge(attributes, SimpleTextAttributes.EXCLUDED_ATTRIBUTES);
        }
        append(PsiUtil.getFieldName(element), attributes);
        String tooltip = element != null ? element.getText() : "";
        setToolTipText(tooltip);
        append("   " + field.visibility.name().toLowerCase() + " - " + tooltip, SimpleTextAttributes.merge(SMALL, SimpleTextAttributes.GRAYED_ATTRIBUTES));
    }

    private <T> T getField(Object value, Class<T> clazz) {
        if (value instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (clazz.isInstance(userObject)) {
                return (T) userObject;
            }
        }
        return null;
    }
}
