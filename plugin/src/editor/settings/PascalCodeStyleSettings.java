package com.siberika.idea.pascal.editor.settings;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2017
 */
public class PascalCodeStyleSettings extends CustomCodeStyleSettings {
    public boolean SPACE_AROUND_EQ_TYPE_DECL = false;
    public boolean BEGIN_ON_NEW_LINE = true;

    PascalCodeStyleSettings(CodeStyleSettings container) {
        super("PascalCodeStyleSettings", container);
    }
}
