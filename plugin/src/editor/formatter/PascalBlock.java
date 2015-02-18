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
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
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

    private static final TokenSet TOKENS_PARENT_INDENTED = TokenSet.create(PasTypes.STATEMENT, PasTypes.VAR_DECLARATION, PasTypes.CONST_DECLARATION, PasTypes.TYPE_DECLARATION,
            PasTypes.CLASS_TYPE_DECL, PasTypes.RECORD_DECL, PasTypes.OBJECT_DECL, PasTypes.CLASS_HELPER_DECL, PasTypes.INTERFACE_TYPE_DECL, PasTypes.RECORD_HELPER_DECL,
            PasTypes.CLASS_FIELD, PasTypes.EXPORTED_ROUTINE, PasTypes.CLASS_PROPERTY, PasTypes.CLASS_METHOD, PasTypes.CLASS_METHOD_RESOLUTION);
    private static final TokenSet TOKENS_NOLFAFTERSEMI = TokenSet.create(PasTypes.FORMAL_PARAMETER_LIST, PasTypes.EXPORTED_ROUTINE);
    private static final TokenSet TOKENS_ENTER_INDENTED = TokenSet.create(PasTypes.STATEMENT);

    private static final TokenSet TOKENS_USED = getTokensUsed();
    private static final TokenSet TOKEN_STRUCT_FILTERED = TokenSet.create();

    private static TokenSet getTokensUsed() {
        TokenSet result = TokenSet.create(PasTypes.MODULE, PasTypes.NAMESPACE_IDENT, PasTypes.SUB_IDENT, PasTypes.TYPE, PasTypes.VAR, PasTypes.CONST);
        result = TokenSet.andNot(result, TOKENS_ENTER_INDENTED);
        result = TokenSet.andNot(result, TOKENS_NOLFAFTERSEMI);
        result = TokenSet.andNot(result, TOKENS_ENTER_INDENTED);
        result = TokenSet.andNot(result, PascalFormatter.TOKENS_USED);
        return result;
    }

    public PascalBlock(@Nullable PascalBlock parent, @NotNull ASTNode node, @NotNull CodeStyleSettings settings, @Nullable Wrap wrap, @Nullable Alignment alignment, Indent indent) {
        super(node, wrap, null);
        mySpacingBuilder = PascalFormatter.createSpacingBuilder(settings);
        mySettings = settings;
        myParent = parent;
        myIndent = indent;
        //System.out.println("block: " + block2Str(this));
    }

    @Override
    protected List<Block> buildChildren() {
        if (mySubBlocks == null) {
            mySubBlocks = ContainerUtil.mapNotNull(myNode.getChildren(null), new Function<ASTNode, Block>() {
                @Override
                public Block fun(ASTNode node) {
                    if (isWhitespaceOrEmpty(node) || TOKEN_STRUCT_FILTERED.contains(node.getElementType())) {
                        return null;
                    }
                    return makeSubBlock(node);
                }
            });
        }
        /*mySubBlocks = new ArrayList<Block>();
        for (ASTNode node : myNode.getChildren(null)) {
            if (!TOKENS_USED.contains(node.getElementType()) && !isWhitespaceOrEmpty(node)) {
                int minPos = node.getTextRange().getEndOffset();
                int maxPos = node.getTextRange().getStartOffset();
                for (ASTNode ch : node.getChildren(null)) {
                    minPos = minPos > ch.getTextRange().getStartOffset() ? ch.getTextRange().getStartOffset() : minPos;
                    maxPos = maxPos < ch.getTextRange().getEndOffset() ? ch.getTextRange().getEndOffset() : maxPos;
                }
                if ((minPos <= node.getTextRange().getStartOffset() && (maxPos >= node.getTextRange().getEndOffset()))) {
                    for (ASTNode ch : node.getChildren(null)) {
                        mySubBlocks.add(makeSubBlock(ch));
                    }
                }
            } else if (!isWhitespaceOrEmpty(node)) {
                mySubBlocks.add(makeSubBlock(node));
            }
        }*/

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
        if (TOKENS_PARENT_INDENTED.contains(myNode.getElementType()) && (childNode != null)) {
            if ((myNode.getElementType() != PasTypes.STATEMENT) || (childNode.getElementType() != PasTypes.COMPOUND_STATEMENT)) {
                //System.out.println("Parent ind: " + myNode + " . " + childNode);
                return Indent.getNormalIndent();
            }
        }
        return Indent.getNoneIndent();
    }

    @Override
    public Indent getIndent() {
        return myIndent;
    }

    private static boolean isWhitespaceOrEmpty(ASTNode node) {
        return node.getElementType() == TokenType.WHITE_SPACE || node.getTextLength() == 0;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
//        System.out.println("getSpacing: " + block2Str(child1) + ", " + block2Str(child2));
        if ((child1 instanceof PascalBlock) && (((PascalBlock) child1).getNode().getElementType() == PasTypes.SEMI)) {
            if (!TOKENS_NOLFAFTERSEMI.contains(this.getNode().getElementType())) {
                //System.out.println("getSpacing: " + block2Str(this) + " . " + block2Str(child1) + ", " + block2Str(child2));
                return Spacing.createSpacing(0, 0, 1, true, 1);
            } else {
                return Spacing.createSpacing(1, 1, 0, true, 1);
            }
        }
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
        if ((childNode != null) && (childNode.getTreeParent() != null)) {
            PsiElement psi = childNode.getPsi();
            if (TOKENS_ENTER_INDENTED.contains(childNode.getTreeParent().getElementType())) {
                //System.out.println("Enter ind: " + myNode + " . " + childNode);
                return Indent.getNormalIndent();
            }
            if (psi instanceof PasEntityScope) {
                //System.out.println("Enter scoped: " + myNode + " . " + childNode);
                return Indent.getContinuationIndent();
            }
        }
        //System.out.println("!Enter ind: " + myNode + " . " + childNode);
        return Indent.getNoneIndent();
    }

}
