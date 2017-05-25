package com.siberika.idea.pascal.editor.settings;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2017
 */
public class PascalCodeStyleSettings extends CustomCodeStyleSettings {
    public boolean SPACE_AROUND_EQ_DECL = true;
    public boolean BEGIN_ON_NEW_LINE = true;
    public boolean INDENT_BEGIN_END = false;
    public boolean SPACE_AROUND_RANGE = false;
    public boolean KEEP_SIMPLE_SECTIONS_IN_ONE_LINE = false;

    PascalCodeStyleSettings(CodeStyleSettings container) {
        super("PascalCodeStyleSettings", container);
    }
}
