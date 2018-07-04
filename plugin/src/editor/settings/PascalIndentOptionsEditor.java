package com.siberika.idea.pascal.editor.settings;

import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.siberika.idea.pascal.PascalBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PascalIndentOptionsEditor extends SmartIndentOptionsEditor {

    private JCheckBox myCbIndentBeginend;

    @Override
    protected void addComponents() {
        super.addComponents();
        addCustomOptions();
    }

    private void addCustomOptions() {
        myCbIndentBeginend = new JCheckBox(PascalBundle.message("style.settings.indent.begin_end"));
        add(myCbIndentBeginend);
    }

    @Override
    public boolean isModified(CodeStyleSettings settings, CommonCodeStyleSettings.IndentOptions options) {
        final PascalCodeStyleSettings pascalSettings = settings.getCustomSettings(PascalCodeStyleSettings.class);
        return super.isModified(settings, options) || isFieldModified(myCbIndentBeginend, pascalSettings.INDENT_BEGIN_END);
    }

    @Override
    public void apply(CodeStyleSettings settings, CommonCodeStyleSettings.IndentOptions options) {
        super.apply(settings, options);
        final PascalCodeStyleSettings pascalSettings = settings.getCustomSettings(PascalCodeStyleSettings.class);
        pascalSettings.INDENT_BEGIN_END = myCbIndentBeginend.isSelected();
    }

    @Override
    public void reset(@NotNull CodeStyleSettings settings, @NotNull CommonCodeStyleSettings.IndentOptions options) {
        super.reset(settings, options);
        final PascalCodeStyleSettings pascalSettings = settings.getCustomSettings(PascalCodeStyleSettings.class);
        myCbIndentBeginend.setSelected(pascalSettings.INDENT_BEGIN_END);
    }
}
