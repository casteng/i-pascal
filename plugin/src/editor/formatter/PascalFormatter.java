package com.siberika.idea.pascal.editor.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.settings.PascalCodeStyleSettings;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
public class PascalFormatter implements FormattingModelBuilder {
    /*static final TokenSet TOKENS_USED = TokenSet.create(
            PasTypes.COMMA, PasTypes.NAMED_IDENT, PasTypes.LPAREN, PasTypes.ASSIGN_OP, PasTypes.ASSIGN_PART, PasTypes.BEGIN,
            PasTypes.COMPOUND_STATEMENT, PasTypes.VAR, PasTypes.CONST, PasTypes.TYPE, PasTypes.VAR_SECTION, PasTypes.CONST_SECTION, PasTypes.TYPE_SECTION,
            PasTypes.COLON, PasTypes.TYPE_DECL, PasTypes.BLOCK_BODY, PasTypes.STATEMENT, PasTypes.END,
            PasTypes.INTERFACE, PasTypes.IMPLEMENTATION, PasTypes.BLOCK_BODY
    );*/

    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        CommonCodeStyleSettings set = settings.getCommonSettings(PascalLanguage.INSTANCE);
        Wrap wrap = set.WRAP_LONG_LINES ? Wrap.createWrap(WrapType.NORMAL, true) : Wrap.createWrap(WrapType.NONE, true);
        Block block = new PascalBlock(element.getNode(), settings, wrap, null);
        return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(), block, settings);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
        final PascalCodeStyleSettings pascalSettings = settings.getCustomSettings(PascalCodeStyleSettings.class);
        final CommonCodeStyleSettings commonSettings = settings.getCommonSettings(PascalLanguage.INSTANCE);
        final int spCommaB = commonSettings.SPACE_BEFORE_COMMA ? 1 : 0;
        final int spCommaA = commonSettings.SPACE_AFTER_COMMA ? 1 : 0;
        final int spCommaTypeArgA = commonSettings.SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS ? 1 : 0;
        final int spColonB = commonSettings.SPACE_BEFORE_COLON ? 1 : 0;
        final int spColonA = commonSettings.SPACE_AFTER_COLON ? 1 : 0;
        final int spAssignAr = commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS ? 1 : 0;
        final int spOps = commonSettings.SPACE_AROUND_LOGICAL_OPERATORS ? 1 : 0;
        final int spSemiA = commonSettings.SPACE_AFTER_SEMICOLON ? 1 : 0;
        final int spSemiB = commonSettings.SPACE_BEFORE_SEMICOLON ? 1 : 0;
        final int spBraketWi = commonSettings.SPACE_WITHIN_BRACKETS ? 1 : 0;
        final int spGroupParenWi = commonSettings.SPACE_WITHIN_PARENTHESES ? 1 : 0;
        final int spRoutineParenWi = commonSettings.SPACE_WITHIN_METHOD_PARENTHESES ? 1 : 0;
        final int spCallParenWi = commonSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES ? 1 : 0;

        final int spDeclEqAr = pascalSettings.SPACE_AROUND_EQ_DECL ? 1 : 0;
        final int spRangeAr = pascalSettings.SPACE_AROUND_RANGE ? 1 : 0;

        final boolean keepBreaks = commonSettings.KEEP_LINE_BREAKS;
        final boolean keepSectionsOneLine = pascalSettings.KEEP_SIMPLE_SECTIONS_IN_ONE_LINE;

        return new SpacingBuilder(settings, PascalLanguage.INSTANCE)
                .afterInside(PasTypes.COMMA, PasTypes.GENERIC_DEFINITION).spacing(spCommaTypeArgA, spCommaTypeArgA, 0, keepBreaks, 0)
                .after(PasTypes.COMMA).spacing(spCommaA, spCommaA, 0, keepBreaks, 1)
                .before(PasTypes.COMMA).spacing(spCommaB, spCommaB, 0, keepBreaks, 0)
                .after(PasTypes.COLON).spacing(spColonA, spColonA, 0, false, 0)
                .before(PasTypes.COLON).spacing(spColonB, spColonB, 0, false, 0)

                .after(PasTypes.SEMI).spacing(spSemiA, spSemiA, 0, keepBreaks, 2)
                .before(PasTypes.SEMI).spacing(spSemiB, spSemiB, 0, false, 0)

                .after(PasTypes.ASSIGN_OP).spacing(spAssignAr, spAssignAr, 0, keepBreaks, 0)
                .before(PasTypes.ASSIGN_PART).spacing(spAssignAr, spAssignAr, 0, keepBreaks, 0)

                .after(PasTypes.ADD_OP).spacing(spOps, spOps, 0, keepBreaks, 0)
                .before(PasTypes.ADD_OP).spacing(spOps, spOps, 0, keepBreaks, 0)
                .after(PasTypes.REL_OP).spacing(spOps, spOps, 0, keepBreaks, 0)
                .before(PasTypes.REL_OP).spacing(spOps, spOps, 0, keepBreaks, 0)
                .after(PasTypes.MUL_OP).spacing(spOps, spOps, 0, keepBreaks, 0)
                .before(PasTypes.MUL_OP).spacing(spOps, spOps, 0, keepBreaks, 0)

                .after(PasTypes.RANGE).spacing(spRangeAr, spRangeAr, 0, false, 0)
                .before(PasTypes.RANGE).spacing(spRangeAr, spRangeAr, 0, false, 0)

                .after(PasTypes.LBRACK).spacing(spBraketWi, spBraketWi, 0, keepBreaks, 0)
                .before(PasTypes.RBRACK).spacing(spBraketWi, spBraketWi, 0, keepBreaks, 0)

                .afterInside(PasTypes.LPAREN, PasTypes.PAREN_EXPR).spacing(spGroupParenWi, spGroupParenWi, 0, keepBreaks, 0)
                .beforeInside(PasTypes.RPAREN, PasTypes.PAREN_EXPR).spacing(spGroupParenWi, spGroupParenWi, 0, keepBreaks, 0)

                .afterInside(PasTypes.LPAREN, PasTypes.FORMAL_PARAMETER_SECTION).spacing(spRoutineParenWi, spRoutineParenWi, 0, keepBreaks, 0)
                .beforeInside(PasTypes.RPAREN, PasTypes.FORMAL_PARAMETER_SECTION).spacing(spRoutineParenWi, spRoutineParenWi, 0, keepBreaks, 0)

                .afterInside(PasTypes.LPAREN, PasTypes.ARGUMENT_LIST).spacing(spCallParenWi, spCallParenWi, 0, keepBreaks, 0)
                .beforeInside(PasTypes.RPAREN, PasTypes.ARGUMENT_LIST).spacing(spCallParenWi, spCallParenWi, 0, keepBreaks, 0)

                .after(PasTypes.EQ).spacing(spDeclEqAr, spDeclEqAr, 0, keepBreaks, 0)
                .before(PasTypes.EQ).spacing(spDeclEqAr, spDeclEqAr, 0, keepBreaks, 0)

                .afterInside(PascalLexer.VAR_KEY, PasTypes.VAR_SECTION).spacing(1, 1, keepSectionsOneLine ? 0 : 1, keepBreaks, 0)
                .afterInside(PascalLexer.CONST_KEY, PasTypes.CONST_SECTION).spacing(1, 1, keepSectionsOneLine ? 0 : 1, keepBreaks, 0)
                .afterInside(PasTypes.TYPE, PasTypes.TYPE_SECTION).spacing(1, 1, keepSectionsOneLine ? 0 : 1, keepBreaks, 0)

                .around(PasTypes.ELSE).spacing(1, 1, commonSettings.ELSE_ON_NEW_LINE ? 1 : 0, keepBreaks, 0)

                .afterInside(PasTypes.INTERFACE, PasTypes.UNIT_INTERFACE).blankLines(commonSettings.BLANK_LINES_BEFORE_IMPORTS)
                .after(PasTypes.USES_CLAUSE).blankLines(commonSettings.BLANK_LINES_AFTER_IMPORTS)
                .after(PasTypes.IMPLEMENTATION).blankLines(commonSettings.BLANK_LINES_BEFORE_IMPORTS)
                .after(PasTypes.BLOCK_BODY).blankLines(1)

                .around(PasTypes.TYPE_DECLARATION).blankLines(commonSettings.BLANK_LINES_AROUND_CLASS)
                .around(PascalBlock.DECL_SECTIONS).spacing(1, 1, keepSectionsOneLine ? 0 : 1,
                        true, commonSettings.KEEP_BLANK_LINES_IN_DECLARATIONS)
                .after(PasTypes.EXPORTED_ROUTINE).blankLines(commonSettings.BLANK_LINES_AROUND_METHOD_IN_INTERFACE)
                .after(PascalBlock.ROUTINE_IMPLS).blankLines(commonSettings.BLANK_LINES_AROUND_METHOD)
                ;
    }
}
