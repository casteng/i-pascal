package com.siberika.idea.pascal.editor.settings;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalLanguage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2017
 */
public class PascalLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    private static final Logger LOG = Logger.getInstance(PascalLanguageCodeStyleSettingsProvider.class.getName());

    private static final String GROUP_AROUND_OPERATORS = "Around Operators";

    @NotNull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            return readCodeSample(settingsType.name());
        } else {
            return readCodeSample("default");
        }
    }

    private String readCodeSample(String name) {
        InputStream is = getClass().getResourceAsStream(String.format("/samples/%s.pas", name));
        try {
            return StreamUtil.readText(is, "UTF-8");
        } catch (IOException e) {
            LOG.warn("ERROR: ", e);
            return "";
        }
    }

    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new PascalIndentOptionsEditor();
    }

    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(getLanguage());
        CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.initIndentOptions();
        indentOptions.INDENT_SIZE = 2;
        indentOptions.CONTINUATION_INDENT_SIZE = 4;
        indentOptions.TAB_SIZE = 2;

        return defaultSettings;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions(
                    "SPACE_AROUND_ASSIGNMENT_OPERATORS",
                    "SPACE_AROUND_LOGICAL_OPERATORS",

                    "SPACE_WITHIN_PARENTHESES",
                    "SPACE_WITHIN_METHOD_CALL_PARENTHESES",
                    "SPACE_WITHIN_METHOD_PARENTHESES",
                    "SPACE_WITHIN_BRACKETS",

                    "SPACE_AFTER_COMMA",
                    "SPACE_BEFORE_COMMA",
                    "SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS",

                    "SPACE_AFTER_SEMICOLON",
                    "SPACE_BEFORE_SEMICOLON",

                    "SPACE_BEFORE_COLON",
                    "SPACE_AFTER_COLON"
                    );
            consumer.moveStandardOption("SPACE_BEFORE_COLON", PascalBundle.message("style.settings.spaces.group.declarations"));
            consumer.moveStandardOption("SPACE_AFTER_COLON", PascalBundle.message("style.settings.spaces.group.declarations"));

            consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", PascalBundle.message("style.settings.spaces.around.assignment"));
            consumer.renameStandardOption("SPACE_AROUND_LOGICAL_OPERATORS", PascalBundle.message("style.settings.spaces.around.operations"));

            consumer.renameStandardOption("SPACE_WITHIN_METHOD_PARENTHESES", PascalBundle.message("style.settings.spaces.within.routine.decl.paren"));
            consumer.renameStandardOption("SPACE_WITHIN_METHOD_CALL_PARENTHESES", PascalBundle.message("style.settings.spaces.within.routine.call.paren"));

            consumer.showCustomOption(PascalCodeStyleSettings.class, "SPACE_AROUND_EQ_DECL", PascalBundle.message("style.settings.spaces.around.decleq"), GROUP_AROUND_OPERATORS);
            consumer.showCustomOption(PascalCodeStyleSettings.class, "SPACE_AROUND_RANGE", PascalBundle.message("style.settings.spaces.around.range"), GROUP_AROUND_OPERATORS);
        } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showStandardOptions(
                    "RIGHT_MARGIN",
                    "WRAP_ON_TYPING",

                    "KEEP_LINE_BREAKS",
                    "KEEP_SIMPLE_BLOCKS_IN_ONE_LINE",
                    "KEEP_SIMPLE_METHODS_IN_ONE_LINE",
//                    "KEEP_SIMPLE_LAMBDAS_IN_ONE_LINE",
                    "KEEP_MULTIPLE_EXPRESSIONS_IN_ONE_LINE",

                    "WRAP_LONG_LINES",
//                    "WRAP_COMMENTS",

//                    "CALL_PARAMETERS_WRAP",
//                    "ALIGN_MULTILINE_PARAMETERS_IN_CALLS",

//                    "METHOD_PARAMETERS_WRAP",
//                    "ALIGN_MULTILINE_PARAMETERS",

//                    "METHOD_CALL_CHAIN_WRAP",
//                    "ALIGN_MULTILINE_CHAINED_METHODS",

                    "ELSE_ON_NEW_LINE"

//                    "BINARY_OPERATION_WRAP",
//                    "ALIGN_MULTILINE_BINARY_OPERATION",
//                    "BINARY_OPERATION_SIGN_ON_NEXT_LINE",

//                    "ENUM_CONSTANTS_WRAP"
            );

            consumer.renameStandardOption("KEEP_SIMPLE_METHODS_IN_ONE_LINE", PascalBundle.message("style.settings.wrap.keep.simple.routines"));
            consumer.renameStandardOption("CALL_PARAMETERS_WRAP", PascalBundle.message("style.settings.wrap.actual.arguments"));
            consumer.renameStandardOption("METHOD_PARAMETERS_WRAP", PascalBundle.message("style.settings.wrap.formal.parameters"));
            consumer.renameStandardOption("METHOD_CALL_CHAIN_WRAP", PascalBundle.message("style.settings.wrap.call.chains"));

            consumer.showCustomOption(PascalCodeStyleSettings.class, "BEGIN_ON_NEW_LINE", PascalBundle.message("style.settings.wrap.begin.newline"),
                    null, CodeStyleSettingsCustomizable.OptionAnchor.BEFORE, "WRAP_LONG_LINES");
            consumer.showCustomOption(PascalCodeStyleSettings.class, "KEEP_SIMPLE_SECTIONS_IN_ONE_LINE", PascalBundle.message("style.settings.wrap.keep.simple.sections"),
                    "Keep when reformatting", CodeStyleSettingsCustomizable.OptionAnchor.AFTER, "KEEP_SIMPLE_METHODS_IN_ONE_LINE");
        } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions(
                    "KEEP_BLANK_LINES_IN_DECLARATIONS",
                    "KEEP_BLANK_LINES_IN_CODE",
                    "KEEP_BLANK_LINES_BEFORE_RBRACE",

                    "BLANK_LINES_AFTER_PACKAGE",
                    "BLANK_LINES_BEFORE_IMPORTS",
                    "BLANK_LINES_AFTER_IMPORTS",
                    "BLANK_LINES_AROUND_CLASS",
                    "BLANK_LINES_AROUND_METHOD",
                    "BLANK_LINES_AROUND_FIELD",
                    "BLANK_LINES_AROUND_METHOD_IN_INTERFACE",
                    "BLANK_LINES_AFTER_CLASS_HEADER"
            );

            consumer.renameStandardOption("KEEP_BLANK_LINES_BEFORE_RBRACE", PascalBundle.message("style.settings.blanklines.before.end"));
            
            consumer.renameStandardOption("BLANK_LINES_AFTER_PACKAGE", PascalBundle.message("style.settings.blanklines.after.modulehead"));
            consumer.renameStandardOption("BLANK_LINES_BEFORE_IMPORTS", PascalBundle.message("style.settings.blanklines.before.uses"));
            consumer.renameStandardOption("BLANK_LINES_AFTER_IMPORTS", PascalBundle.message("style.settings.blanklines.after.uses"));
            consumer.renameStandardOption("BLANK_LINES_AROUND_METHOD", PascalBundle.message("style.settings.blanklines.around.routine.definition"));
            consumer.renameStandardOption("BLANK_LINES_AROUND_METHOD_IN_INTERFACE", PascalBundle.message("style.settings.blanklines.around.routine.declaration"));
        } else if (settingsType == SettingsType.INDENT_SETTINGS) {
            consumer.showCustomOption(PascalCodeStyleSettings.class, "INDENT_BEGIN_END", "style.settings.indent.begin_end", "test");
        }
    }

}
