package com.siberika.idea.pascal.debugger.settings;

import com.siberika.idea.pascal.PascalBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DataViewsConfigurable extends AbstractConfigurable<PascalDebuggerViewSettings> {

    public DataViewsConfigurable(String bundlePrefix) {
        super(bundlePrefix);
    }

    @Override
    public String getDisplayName() {
        return PascalBundle.message("debug.settings.general");
    }

    @Override
    public JComponent createComponent() {
        return createOptionsPanel(PascalDebuggerViewSettings.class);
    }

    @Override
    public void apply() {
        doApply(PascalDebuggerViewSettings.getInstance());
    }

    @Override
    public void reset() {
        doReset(PascalDebuggerViewSettings.getInstance());
    }

    @Override
    public boolean isModified() {
        return !PascalDebuggerViewSettings.getInstance().equals(doApply(new PascalDebuggerViewSettings()));
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    @NotNull
    public String getHelpTopic() {
        return "Debugger_Pascal_Data_Views";
    }

    @Override
    @NotNull
    public String getId() {
        return getHelpTopic();
    }

}
