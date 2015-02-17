package com.siberika.idea.pascal.editor.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 02/10/2013
 */
public class PascalBlock extends AbstractBlock implements Block {

    private List<Block> mySubBlocks = null;
    private final PascalBlock myParent;
    private final SpacingBuilder mySpacingBuilder;
    private final CodeStyleSettings mySettings;
    private final Indent myIndent;

    private static final TokenSet TOKENS_INDENTED = TokenSet.create(PasTypes.STATEMENT_LIST, PasTypes.VAR_DECLARATION, PasTypes.CONST_DECLARATION, PasTypes.TYPE_DECLARATION, PasTypes.CLASS_FIELD);

    public PascalBlock(@Nullable PascalBlock parent, @NotNull ASTNode node, @NotNull CodeStyleSettings settings, @Nullable Wrap wrap, @Nullable Alignment alignment, Indent indent) {
        super(node, wrap, null);
        mySpacingBuilder = PascalFormatter.createSpacingBuilder(settings);
        mySettings = settings;
        myParent = parent;
        myIndent = indent;
    }

    @Override
    protected List<Block> buildChildren() {
        if (mySubBlocks == null) {
            mySubBlocks = ContainerUtil.mapNotNull(myNode.getChildren(null), new Function<ASTNode, Block>() {
                @Override
                public Block fun(ASTNode node) {
                    if (isWhitespaceOrEmpty(node)) {
                        return null;
                    }
                    return makeSubBlock(node);
                }
            });
        }
        return mySubBlocks;
    }

    private Block makeSubBlock(@NotNull ASTNode childNode) {
        Indent indent = getBlockIndent(childNode);

        Alignment alignment = Alignment.createAlignment(true);
        if (myParent != null) {
            alignment = myParent.getAlignment();
        } else {
            System.out.println("new alignment: " + childNode);
        }

        Wrap wrap = Wrap.createWrap(WrapType.NORMAL, false);

        return new PascalBlock(this, childNode, mySettings, wrap, alignment, indent);
    }

    private Indent getBlockIndent(@Nullable ASTNode childNode) {
        if (TOKENS_INDENTED.contains(myNode.getElementType())) {
            return Indent.getNormalIndent();
        }
        return Indent.getNoneIndent();
    }

    @Override
    public Indent getIndent() {
        return myIndent;
    }

    public static boolean hasElementType(@NotNull ASTNode node, @NotNull TokenSet set) {
        return set.contains(node.getElementType());
    }

    private static boolean isWhitespaceOrEmpty(ASTNode node) {
        return node.getElementType() == TokenType.WHITE_SPACE || node.getTextLength() == 0;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        System.out.println("getSpacing: " + block2Str(child1) + ", " + block2Str(child2));
        return mySpacingBuilder.getSpacing(this, child1, child2);
    }

    private String block2Str(Block r) {
        if (null == r) {
            return "[-]";
        }
        if (r instanceof PascalBlock) {
            return ((PascalBlock) r).getNode().toString();
        }
        return "[" + r.getTextRange().getStartOffset() + ".." + r.getTextRange().getEndOffset() + "]";
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }

    @Override
    @NotNull
    public ChildAttributes getChildAttributes(final int newChildIndex) {
        return new ChildAttributes(getChildBlockIndent(myNode), null);
    }

    private Indent getChildBlockIndent(ASTNode childNode) {
        if (childNode != null) {
            if (TOKENS_INDENTED.contains(childNode.getElementType())) {
                return Indent.getNormalIndent();
            }
        }
        return Indent.getNoneIndent();
    }

}
