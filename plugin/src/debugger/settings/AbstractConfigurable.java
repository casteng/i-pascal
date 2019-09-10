package com.siberika.idea.pascal.debugger.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.JBColor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.ui.JBUI;
import com.siberika.idea.pascal.PascalBundle;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractConfigurable<T> implements SearchableConfigurable {

    private final String bundlePrefix;

    protected AbstractConfigurable(String bundlePrefix) {
        this.bundlePrefix = bundlePrefix;
    }

    public enum Type {BOOLEAN, INTEGER, STRING}

    protected Map<String, Control> controlMap;

    protected JPanel createOptionsPanel(Class<T> clazz) {
        controlMap = new LinkedHashMap<>();
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(JBColor.border()));

        List<Field> fields = ReflectionUtil.collectFields(clazz);
        panel.setLayout(new GridLayoutManager(fields.size(), 2, JBUI.emptyInsets(), 1, 1));
        int row = 0;
        for (Field field : fields) {
            Control control = new Control(field);
            createComponent(panel, control, row++);
            controlMap.put(control.fieldName, control);
        }
        outerPanel.add(panel, BorderLayout.PAGE_START);
        return outerPanel;
    }

    protected T doApply(T instance) {
        for (Control control : controlMap.values()) {
            ReflectionUtil.setField(instance.getClass(), instance, null, control.fieldName, UIUtils.getValue(control.component, control.fieldType));
        }
        return instance;
    }

    protected void doReset(T instance) {
        for (Control control : controlMap.values()) {
            Object value = ReflectionUtil.getField(instance.getClass(), instance, null, control.fieldName);
            UIUtils.setValue(control.component, control.fieldType, value);
        }
    }

    @Override
    public void disposeUIResources() {
        controlMap = null;
    }

    private void createComponent(JPanel panel, Control control, int row) {
        if (control.fieldType == Type.BOOLEAN) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setText(PascalBundle.message(bundlePrefix + control.fieldName));
            control.component = checkBox;
            addLabel(panel, "", row);
        } else {
            addLabel(panel, PascalBundle.message(bundlePrefix + control.fieldName), row);
            JTextField edit = new JTextField();
            control.component = edit;
        }
        panel.add(control.component, new GridConstraints(row, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));
    }

    private void addLabel(JPanel panel, String caption, int row) {
        final JLabel label1 = new JLabel(caption);
        panel.add(label1, new GridConstraints(row, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 1, false));
    }

    private static class Control {
        private final String fieldName;
        private final Type fieldType;
        private JComponent component;

        public Control(Field field) {
            this.fieldName = field.getName();
            this.fieldType = convertType(field.getType());
        }

        private Type convertType(Class<?> type) {
            if (isInteger(type)) {
                return Type.INTEGER;
            } else if (isBoolean(type)) {
                return Type.BOOLEAN;
            } else {
                return Type.STRING;
            }
        }

        private boolean isInteger(Class<?> type) {
            return Integer.class.equals(type) || "int".equals(type.getName())
                    || Long.class.equals(type) || "long".equals(type.getName())
                    || Byte.class.equals(type) || "byte".equals(type.getName());
        }

        private boolean isBoolean(Class<?> type) {
            return Boolean.class.equals(type) || "boolean".equals(type.getName());
        }
    }
}
