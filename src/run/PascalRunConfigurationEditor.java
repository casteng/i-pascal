package com.siberika.idea.pascal.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author: George Bakhtadze
 * Date: 06/01/2013
 */
public class PascalRunConfigurationEditor extends SettingsEditor<PascalRunConfiguration> {
    private PascalRunConfigurationForm myForm;

    public PascalRunConfigurationEditor(PascalRunConfiguration batchRunConfiguration) {
        this.myForm = new PascalRunConfigurationForm(batchRunConfiguration);
    }

    @Override
    protected void resetEditorFrom(PascalRunConfiguration runConfiguration) {
        PascalRunConfiguration.copyParams(runConfiguration, myForm);
    }

    @Override
    protected void applyEditorTo(PascalRunConfiguration runConfiguration) throws ConfigurationException {
        PascalRunConfiguration.copyParams(myForm, runConfiguration);
    }

    @Override
    @NotNull
    protected JComponent createEditor() {
        return myForm.getRootPanel();
    }

    @Override
    protected void disposeEditor() {
        myForm = null;
    }
}