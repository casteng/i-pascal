package com.siberika.idea.pascal.editor.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 02/10/2013
 */
public class PascalBlock extends AbstractBlock implements Block {

    private List<Block> mySubBlocks = null;
    private final SpacingBuilder mySpacingBuilder;
    private final CodeStyleSettings mySettings;
    private final Indent myIndent;

    private static final TokenSet TOKENS_PARENT_INDENTED = TokenSet.create(PasTypes.STATEMENT, PasTypes.VAR_DECLARATION, PasTypes.CONST_DECLARATION, PasTypes.TYPE_DECLARATION,
            PasTypes.CLASS_TYPE_DECL, PasTypes.RECORD_DECL, PasTypes.OBJECT_DECL, PasTypes.CLASS_HELPER_DECL, PasTypes.INTERFACE_TYPE_DECL, PasTypes.RECORD_HELPER_DECL,
            PasTypes.CLASS_FIELD, PasTypes.EXPORTED_ROUTINE, PasTypes.CLASS_PROPERTY, PasTypes.CLASS_METHOD_RESOLUTION, PasTypes.USES_CLAUSE, PasTypes.RECORD_VARIANT,
            PasTypes.ENUM_TYPE);

    private static final TokenSet TOKENS_NO_LF_AFTER_SEMI = TokenSet.create(PasTypes.FORMAL_PARAMETER_LIST, PasTypes.EXPORTED_ROUTINE,
            PasTypes.CLASS_PROPERTY_SPECIFIER, PasTypes.PROCEDURE_REFERENCE);

    private static final TokenSet TOKENS_COMMENT = TokenSet.create(PasTypes.COMMENT, PasTypes.CT_DEFINE, PasTypes.CT_ELSE, PasTypes.CT_ENDIF, PasTypes.CT_IF,
            PasTypes.CT_IFDEF, PasTypes.CT_IFNDEF, PasTypes.CT_IFOPT, PasTypes.CT_UNDEFINE);

    private static final TokenSet TOKENS_ENTER_INDENTED =
            TokenSet.create(PasTypes.VAR_SECTION, PasTypes.CONST_SECTION, PasTypes.TYPE_SECTION, PasTypes.USES_CLAUSE, PasTypes.COMPOUND_STATEMENT);

    private static final TokenSet TOKEN_COMMENT_NORMALINDENT = TokenSet.create(PasTypes.COMPOUND_STATEMENT, PasTypes.USES_CLAUSE);

    private static final Map<IElementType, IElementType> TOKEN_UNINDENTED_MAP = getTokenUnindentedMap();

    private static Map<IElementType, IElementType> getTokenUnindentedMap() {
        Map<IElementType, IElementType> result = new HashMap<IElementType, IElementType>();
        result.put(PasTypes.STATEMENT, PasTypes.COMPOUND_STATEMENT);
        result.put(PasTypes.USES_CLAUSE, PasTypes.USES);
        return result;
    }

    private static final TokenSet TOKEN_STRUCT_FILTERED = TokenSet.create();

/*    private static TokenSet getTokensUsed() {
        TokenSet result = TokenSet.create(PasTypes.MODULE, PasTypes.NAMESPACE_IDENT, PasTypes.SUB_IDENT, PasTypes.TYPE, PasTypes.VAR, PasTypes.CONST);
        result = TokenSet.andNot(result, TOKENS_ENTER_INDENTED);
        result = TokenSet.andNot(result, TOKENS_NO_LF_AFTER_SEMI);
        result = TokenSet.andNot(result, TOKENS_ENTER_INDENTED);
        result = TokenSet.andNot(result, PascalFormatter.TOKENS_USED);
        return result;
    }*/

    public PascalBlock(@NotNull ASTNode node, @NotNull CodeStyleSettings settings, @Nullable Wrap wrap, Indent indent) {
        super(node, wrap, null);
        mySpacingBuilder = PascalFormatter.createSpacingBuilder(settings);
        mySettings = settings;
        myIndent = indent;
//        System.out.println("block: " + block2Str(this));
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

/*        Alignment alignment = Alignment.createAlignment(true);
        if (myParent != null) {
            alignment = myParent.getAlignment();
        }*/

        Wrap wrap = Wrap.createWrap(WrapType.NORMAL, false);

        return new PascalBlock(childNode, mySettings, wrap, indent);
    }

    private Indent getBlockIndent(@Nullable ASTNode childNode) {
        if (childNode != null) {
            if (TOKENS_COMMENT.contains(childNode.getElementType())) {
                System.out.println("Comment ind: " + myNode + " . " + childNode);
                // Not move at leftmost position, indent usually, not indent in already indented contexts such as statement
                Indent commentIndent = Indent.getAbsoluteNoneIndent();
                int curInd = getCurrentIndent(childNode);
                if (curInd > 0) {
                    if (TOKEN_COMMENT_NORMALINDENT.contains(myNode.getElementType())) {
                        commentIndent = Indent.getNormalIndent(false);
                    } else {
                        commentIndent = Indent.getContinuationIndent();
                    }
                }
                return commentIndent;
            }
        }
        if (TOKENS_PARENT_INDENTED.contains(myNode.getElementType()) && (childNode != null)) {
            if (TOKEN_UNINDENTED_MAP.get(myNode.getElementType()) != childNode.getElementType()) {
//                System.out.println("Parent ind: " + myNode + " . " + childNode);
                return Indent.getNormalIndent();
            }
        }
        return Indent.getNoneIndent();
    }

    private int getCurrentIndent(ASTNode childNode) {
        ASTNode prev = childNode.getTreePrev();
        if (prev instanceof PsiWhiteSpace) {
            String text = prev.getText();
            if (text != null) {
                int pos = Math.min(text.length(), text.length() - text.lastIndexOf('\n') - 1);
                System.out.println(String.format("WS text: \"%s\", ind: %d", text, pos));
                return pos;
            }
        }
        return 0;
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
            if (!TOKENS_NO_LF_AFTER_SEMI.contains(this.getNode().getElementType())) {
                if (((PascalBlock) child2).getNode().getElementType() == PasTypes.COMMENT) {
                    return null;
                }
//                System.out.println("getSpacing: " + block2Str(this) + " . " + block2Str(child1) + ", " + block2Str(child2));
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
            return String.format("%s, I: %s", ((PascalBlock) r).getNode().toString(), r.getIndent());
        }
        return String.format("[%d..%d, I: %s]", r.getTextRange().getStartOffset(), r.getTextRange().getEndOffset(), r.getIndent());
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

    private static Indent getChildBlockIndent(ASTNode childNode) {
        if ((childNode != null) && (childNode.getTreeParent() != null)) {
            System.out.println("!Enter ind: " + childNode.getTreeParent() + " . " + childNode +
                    " | " + FormatterUtil.getPreviousNonWhitespaceSibling(childNode) + " | " + FormatterUtil.getPreviousNonWhitespaceLeaf(childNode));
            if ((TOKENS_ENTER_INDENTED.contains(childNode.getElementType())) || (childNode.getTreeParent().getElementType() == PasTypes.STATEMENT)) {
                return Indent.getNormalIndent();
            }
            PsiElement psi = childNode.getPsi();
            if (psi instanceof PascalStructType) {
                //return Indent.getIndent(Indent.Type.NORMAL, true, false);             // When name will be included in struct type declarations
                return Indent.getContinuationIndent(false);
            }
        }
        return Indent.getNoneIndent();
    }

}
