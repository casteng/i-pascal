package com.siberika.idea.pascal.editor.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
public class PascalFormatter implements FormattingModelBuilder {
    private static final TokenSet TOKENS_CLASS_DECL = TokenSet.create(PasTypes.CLASS_FIELD, PasTypes.EXPORTED_ROUTINE, PasTypes.CLASS_PROPERTY, PasTypes.CLASS_METHOD_RESOLUTION, PasTypes.VISIBILITY);

    /*static final TokenSet TOKENS_USED = TokenSet.create(
            PasTypes.COMMA, PasTypes.NAMED_IDENT, PasTypes.LPAREN, PasTypes.ASSIGN_OP, PasTypes.ASSIGN_PART, PasTypes.BEGIN,
            PasTypes.COMPOUND_STATEMENT, PasTypes.VAR, PasTypes.CONST, PasTypes.TYPE, PasTypes.VAR_SECTION, PasTypes.CONST_SECTION, PasTypes.TYPE_SECTION,
            PasTypes.COLON, PasTypes.TYPE_DECL, PasTypes.BLOCK_BODY, PasTypes.STATEMENT, PasTypes.END,
            PasTypes.INTERFACE, PasTypes.IMPLEMENTATION, PasTypes.BLOCK_BODY
    );*/

    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        Block block = new PascalBlock(element.getContainingFile().getNode(), settings, null, null);
        return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(), block, settings);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
        return new SpacingBuilder(settings, PascalLanguage.INSTANCE)
                .between(PasTypes.COMMA, PasTypes.NAMED_IDENT).spacing(1, 1, 0, true, 1)
                .between(PasTypes.COMMA, PasTypes.EXPRESSION).spacing(1, 1, 0, true, 1)
                .between(PasTypes.REF_NAMED_IDENT, PasTypes.LPAREN).spacing(0, 0, 0, true, 1)
                .after(PasTypes.ASSIGN_OP).spacing(1, 1, 0, true, 1)
                .before(PasTypes.ASSIGN_PART).spacing(1, 1, 0, true, 1)

                .after(PasTypes.ADD_OP).spacing(1, 1, 0, true, 1)
                .before(PasTypes.ADD_OP).spacing(1, 1, 0, true, 1)
                .after(PasTypes.REL_OP).spacing(1, 1, 0, true, 1)
                .before(PasTypes.REL_OP).spacing(1, 1, 0, true, 1)
                .after(PasTypes.MUL_OP).spacing(1, 1, 0, true, 1)
                .before(PasTypes.MUL_OP).spacing(1, 1, 0, true, 1)

                .afterInside(PasTypes.BEGIN, PasTypes.COMPOUND_STATEMENT).lineBreakInCode()
                .afterInside(PasTypes.VAR, PasTypes.VAR_SECTION).lineBreakInCode()
                .afterInside(PasTypes.CONST, PasTypes.CONST_SECTION).lineBreakInCode()
                .afterInside(PasTypes.TYPE, PasTypes.TYPE_SECTION).lineBreakInCode()

                .beforeInside(PasTypes.VAR, PasTypes.VAR_SECTION).blankLines(1)
                .beforeInside(PasTypes.CONST, PasTypes.CONST_SECTION).blankLines(1)
                .beforeInside(PasTypes.TYPE, PasTypes.TYPE_SECTION).blankLines(1)
                .between(TOKENS_CLASS_DECL, TOKENS_CLASS_DECL).lineBreakInCode()

                .between(PasTypes.COLON, PasTypes.TYPE_DECL).spacing(1, 1, 0, true, 1)
                .before(PasTypes.BLOCK_BODY).lineBreakInCode()
                .before(PasTypes.COMPOUND_STATEMENT).lineBreakInCode()
                .after(PasTypes.COMPOUND_STATEMENT).lineBreakInCode()
                .between(PasTypes.STATEMENT, PasTypes.END).lineBreakInCode()
                .afterInside(PasTypes.INTERFACE, PasTypes.UNIT_MODULE_HEAD).blankLines(1)
                .after(PasTypes.IMPLEMENTATION).blankLines(1)
                .after(PasTypes.BLOCK_BODY).blankLines(1)

                .after(PasTypes.EQ).spacing(1, 1, 0, true, 1)
                .before(PasTypes.EQ).spacing(0, 1, 0, true, 1)
                ;
    }
}
