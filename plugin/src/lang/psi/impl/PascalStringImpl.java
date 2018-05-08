package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.ProperTextRange;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 21/01/2016
 */
public abstract class PascalStringImpl extends PascalPsiElementImpl implements PsiLanguageInjectionHost, PascalPsiElement {

    public PascalStringImpl(ASTNode node) {
        super(node);
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PascalStringImpl updateText(@NotNull String text) {
        ASTNode valueNode = getNode().getFirstChildNode();
        assert valueNode instanceof LeafElement;
        if (text.length() > 1) {
            text = text.charAt(0) + text.substring(1, text.length()-1).replace("'", "''") + text.charAt(text.length()-1);
        }
        ((LeafElement)valueNode).replaceWithText(text);
        return this;
    }

    @Nullable
    public String getValue() {
        return getText().length() > 2 ? getText().substring(1, getText().length() - 1) : "";
    }

    @Nullable
    public PsiType getType() {
        return null;
    }

    @NotNull
    @Override
    public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new LiteralTextEscaper<PsiLanguageInjectionHost>(this) {

            private String lastDecoded;

            @Override
            public boolean decode(@NotNull TextRange rangeInsideHost, @NotNull StringBuilder outChars) {
                ProperTextRange.assertProperRange(rangeInsideHost);
                String value = myHost.getText().substring(rangeInsideHost.getStartOffset(), rangeInsideHost.getEndOffset());
                lastDecoded = value.replace("''", "'");
                outChars.append(lastDecoded);
                return false;
            }

            @Override
            public int getOffsetInHost(int offsetInDecoded, @NotNull TextRange rangeInsideHost) {
                ProperTextRange.assertProperRange(rangeInsideHost);
                int offset = rangeInsideHost.getStartOffset() + offsetInDecoded;
                int escapedCount = StringUtils.countMatches(lastDecoded.substring(0, offsetInDecoded), "'");
                offset += escapedCount;
                if (offset < rangeInsideHost.getStartOffset()) offset = rangeInsideHost.getStartOffset();
                if (offset > rangeInsideHost.getEndOffset()) offset = rangeInsideHost.getEndOffset();
                return offset;
            }

            @Override
            public boolean isOneLine() {
                return true;
            }
        };
    }

}
