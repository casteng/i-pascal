package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;

import javax.swing.*;
import java.awt.*;

/**
 * Author: George Bakhtadze
 * Date: 18/01/2013
 */
public class PascalSdkConfigUI implements AdditionalDataConfigurable {
    private Sdk sdk;
    JTextField optionsEdit;

    public JComponent createComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText(PascalBundle.message("ui.sdkSettings.compiler.options"));
        panel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        optionsEdit = new JTextField();
        panel.add(optionsEdit, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        return panel;
    }

    public boolean isModified() {
        return (optionsEdit != null) &&
                !optionsEdit.getText().equals(BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS));
    }

    public void apply() throws ConfigurationException {
        if (optionsEdit != null) {
            BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS, optionsEdit.getText());
        }
    }

    public void reset() {
        if (optionsEdit != null) {
            optionsEdit.setText((String) BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS));
        }
    }

    public void disposeUIResources() {
    }

    @Override
    public void setSdk(Sdk sdk) {
        this.sdk = sdk;
    }
}

