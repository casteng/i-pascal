package com.siberika.idea.pascal.editor.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Indent;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
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
    private static final TokenSet TOKENS_BEFORE_BEGIN = TokenSet.create(PasTypes.DO, PasTypes.THEN);

    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        Block block = new PascalBlock(null, element.getNode(), settings, Wrap.createWrap(WrapType.NONE, false), Alignment.createAlignment(), Indent.getNoneIndent());
        return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(), block, settings);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
        return new SpacingBuilder(settings, PascalLanguage.INSTANCE)
                .after(PasTypes.SEMI).lineBreakInCode()
                .between(PasTypes.COMMA, PasTypes.NAMED_IDENT).spacing(1, 1, 0, true, 1)
                .around(PasTypes.ASSIGN_OP).spacing(1, 1, 0, true, 1)

                .afterInside(PasTypes.BEGIN, PasTypes.COMPOUND_STATEMENT).lineBreakInCode()
                .afterInside(PasTypes.VAR, PasTypes.VAR_SECTION).lineBreakInCode()
                .afterInside(PasTypes.CONST, PasTypes.CONST_SECTION).lineBreakInCode()
                .afterInside(PasTypes.TYPE, PasTypes.TYPE_SECTION).lineBreakInCode()

                .beforeInside(PasTypes.VAR, PasTypes.VAR_SECTION).blankLines(1)
                .beforeInside(PasTypes.CONST, PasTypes.CONST_SECTION).blankLines(1)
                .beforeInside(PasTypes.TYPE, PasTypes.TYPE_SECTION).blankLines(1)

                .between(PasTypes.COLON, PasTypes.TYPE_DECL).spacing(1, 1, 0, true, 1)
                .before(PasTypes.BLOCK_BODY).lineBreakInCode()
                .before(PasTypes.COMPOUND_STATEMENT).lineBreakInCode()
                .after(PasTypes.COMPOUND_STATEMENT).lineBreakInCode()
                .between(PasTypes.STATEMENT_LIST, PasTypes.END).lineBreakInCode()
                .after(PasTypes.INTERFACE).blankLines(1)
                .after(PasTypes.IMPLEMENTATION).blankLines(1)
                .after(PasTypes.BLOCK_BODY).blankLines(1)
                ;
    }
}
