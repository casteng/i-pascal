package com.siberika.idea.pascal.debugger.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;

public class UIUtils {
    public static Object getValue(JComponent control, AbstractConfigurable.Type fieldType) {
        if (control instanceof TextFieldWithBrowseButton) {
            return ((TextFieldWithBrowseButton) control).getText();
        } else if (control instanceof JTextField) {
            if (fieldType == AbstractConfigurable.Type.BYTE) {
                return Byte.valueOf(((JTextField) control).getText());
            } else if (fieldType == AbstractConfigurable.Type.INTEGER) {
                return Integer.valueOf(((JTextField) control).getText());
            } else if (fieldType == AbstractConfigurable.Type.LONG) {
                return Long.valueOf(((JTextField) control).getText());
            } else if (fieldType == AbstractConfigurable.Type.SINGLE) {
                return Float.valueOf(((JTextField) control).getText());
            } else if (fieldType == AbstractConfigurable.Type.DOUBLE) {
                return Double.valueOf(((JTextField) control).getText());
            } else {
                return ((JTextField) control).getText();
            }
        } else if (control instanceof JCheckBox) {
            return ((JCheckBox) control).isSelected();
        } else if (control instanceof ComboBox) {
            return ((ComboBox) control).getSelectedItem();
        } else {
            throw new IllegalStateException("getValue: Invalid control: " + ((control != null) ? control.getClass() : "<null>"));
        }
    }

    public static void setValue(JComponent control, AbstractConfigurable.Type fieldType, Object value) {
        if (control instanceof TextFieldWithBrowseButton) {
            ((TextFieldWithBrowseButton) control).setText((String) value);
        } else if (control instanceof JTextField) {
            ((JTextField) control).setText(String.valueOf(value));
        } else if (control instanceof JCheckBox) {
            ((JCheckBox) control).setSelected(Boolean.TRUE.equals(value));
        } else if (control instanceof ComboBox) {
            ((ComboBox) control).setSelectedItem(value);
        } else {
            throw new IllegalStateException("setValue: Invalid control: " + ((control != null) ? control.getClass() : "null"));
        }
    }

    public static boolean isInteger(AbstractConfigurable.Type fieldType) {
        return (fieldType == AbstractConfigurable.Type.BYTE) || (fieldType == AbstractConfigurable.Type.INTEGER) || (fieldType == AbstractConfigurable.Type.LONG);
    }

    public static boolean isFloat(AbstractConfigurable.Type fieldType) {
        return (fieldType == AbstractConfigurable.Type.SINGLE) || (fieldType == AbstractConfigurable.Type.DOUBLE);
    }

}
