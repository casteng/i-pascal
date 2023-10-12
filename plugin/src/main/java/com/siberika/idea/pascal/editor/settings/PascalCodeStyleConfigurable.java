package com.siberika.idea.pascal.editor.settings;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.siberika.idea.pascal.PascalLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2017
 */
public class PascalCodeStyleConfigurable extends CodeStyleAbstractConfigurable {
    public PascalCodeStyleConfigurable(@NotNull CodeStyleSettings settings, CodeStyleSettings cloneSettings) {
        super(settings, cloneSettings, "Pascal");
    }

    @Override
    protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
        return new PascalCodeStyleMainPanel(getCurrentSettings(), settings);
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    private static class PascalCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
        private PascalCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(PascalLanguage.INSTANCE, currentSettings, settings);
        }
    }
}
