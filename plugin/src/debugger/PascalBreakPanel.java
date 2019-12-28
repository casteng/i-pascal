package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase;
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil;
import com.siberika.idea.pascal.PascalBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PascalBreakPanel<T extends PascalLineBreakpointProperties, B extends XBreakpoint<T>> extends XBreakpointCustomPropertiesPanel<B> {
    private JPanel myConditionsPanel;
    private JCheckBox myPassCountCheckbox;
    private JTextField myPassCountField;

    private final Project myProject;

    public PascalBreakPanel(Project project) {
        myProject = project;
        ActionListener updateListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCheckboxes();
            }
        };

        myConditionsPanel = new JPanel();
        myConditionsPanel.setBorder(new LineBorder(JBColor.border()));
        myConditionsPanel.setLayout(new GridLayoutManager(6, 2, JBUI.emptyInsets(), -1, -1));

        JPanel myPassCountFieldPanel = new JPanel();
        myPassCountFieldPanel.setBorder(new LineBorder(JBColor.border()));
        myPassCountFieldPanel.setLayout(new GridLayoutManager(6, 2, JBUI.emptyInsets(), -1, -1));

        myPassCountField = new JTextField();
        myConditionsPanel.add(myPassCountFieldPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));
        myPassCountFieldPanel.add(myPassCountField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));
        myPassCountCheckbox = new JCheckBox(PascalBundle.message("breakpoint.settings.ignore.count"));
        myPassCountCheckbox.addActionListener(updateListener);
        myPassCountFieldPanel.setBorder(JBUI.Borders.emptyLeft(myPassCountCheckbox.getInsets().left));
        myConditionsPanel.add(myPassCountCheckbox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));

        DebuggerUIUtil.focusEditorOnCheck(myPassCountCheckbox, myPassCountField);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return myConditionsPanel;
    }

    @Override
    public boolean isVisibleOnPopup(@NotNull B breakpoint) {
        PascalLineBreakpointProperties properties = breakpoint.getProperties();
        if (properties != null) {
            return properties.isIgnoreCountEnabled();
        }
        return false;
    }

    @Override
    public void saveTo(@NotNull B breakpoint) {
        PascalLineBreakpointProperties properties = breakpoint.getProperties();
        if (properties == null) {
            return;
        }

        boolean changed = false;
        try {
            String text = myPassCountField.getText().trim();
            int filter = !text.isEmpty() ? Integer.parseInt(text) : 0;
            if (filter < 0) {
                filter = 0;
            }
            changed = properties.setIgnoreCount(filter);
        } catch (Exception ignored) {
        }

        changed = changed || properties.setIgnoreCountEnabled(properties.getIgnoreCount() > 0 && myPassCountCheckbox.isSelected());

        if (changed) {
            ((XBreakpointBase)breakpoint).fireBreakpointChanged();
        }
    }

    @Override
    public void loadFrom(@NotNull B breakpoint) {
        PascalLineBreakpointProperties properties = breakpoint.getProperties();
        if (properties != null) {
            if (properties.getIgnoreCount() > 0) {
                myPassCountField.setText(Integer.toString(properties.getIgnoreCount()));
            } else {
                myPassCountField.setText("");
            }
            myPassCountCheckbox.setSelected(properties.isIgnoreCountEnabled());
        }
        updateCheckboxes();
    }

    private void updateCheckboxes() {
        boolean passCountSelected = myPassCountCheckbox.isSelected();
        myPassCountField.setEditable(passCountSelected);
        myPassCountField.setEnabled (passCountSelected);
    }

}