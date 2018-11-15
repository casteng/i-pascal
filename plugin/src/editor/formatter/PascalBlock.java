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
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.settings.PascalCodeStyleSettings;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 02/10/2013
 */
public class PascalBlock extends AbstractBlock implements Block {

    private static final TokenSet ATOMIC_TOKENS = TokenSet.create(PasTypes.REL_OP);

    private static final Wrap WRAP_ALWAYS = Wrap.createWrap(WrapType.ALWAYS, true);

    private List<Block> mySubBlocks = null;
    private final SpacingBuilder mySpacingBuilder;
    private final CodeStyleSettings mySettings;
    private final Indent myIndent;

    private final static TokenSet TOKENS_INDENT_CHILD_NON_RELATIVE = TokenSet.create(
            PasTypes.STATEMENT,
            PasTypes.CLASS_TYPE_DECL, PasTypes.RECORD_DECL, PasTypes.OBJECT_DECL,
            PasTypes.CLASS_HELPER_DECL, PasTypes.INTERFACE_TYPE_DECL, PasTypes.RECORD_HELPER_DECL
    );
    private final static TokenSet TOKENS_INDENT_CHILD = TokenSet.create(
            PasTypes.VAR_DECLARATION, PasTypes.CONST_DECLARATION, PasTypes.TYPE_DECLARATION,
            PasTypes.CLASS_FIELD, PasTypes.EXPORTED_ROUTINE, PasTypes.CLASS_PROPERTY, PasTypes.CLASS_METHOD_RESOLUTION, PasTypes.USES_CLAUSE, PasTypes.RECORD_VARIANT,
            PasTypes.TYPE_DECL,
            PasTypes.FORMAL_PARAMETER_SECTION, PasTypes.FORMAL_PARAMETER,
            PasTypes.ARRAY_TYPE, PasTypes.SUB_RANGE_TYPE,
            PasTypes.ARGUMENT_LIST, PasTypes.ASSIGN_PART, PasTypes.ENUM_TYPE
    );
    private final static TokenSet TOKENS_INDENT_BLOCK = TokenSet.create(
            PasTypes.CASE_ITEM, PasTypes.HANDLER
    );

    private static final TokenSet TOKENS_COMMENT = PascalLexer.COMMENTS;

    private static final TokenSet TOKENS_ENTER_INDENTED = TokenSet.create(
            PasTypes.VAR_SECTION, PasTypes.CONST_SECTION, PasTypes.TYPE_SECTION, PasTypes.USES_CLAUSE,
            PasTypes.COMPOUND_STATEMENT, PasTypes.SUM_EXPR, PasTypes.FULLY_QUALIFIED_IDENT, PasTypes.STATEMENT, PasTypes.EXPRESSION);

    private static final TokenSet TOKEN_COMMENT_NORMALINDENT = TokenSet.create(PasTypes.COMPOUND_STATEMENT, PasTypes.USES_CLAUSE,
            PasTypes.UNIT_INTERFACE, PasTypes.UNIT_IMPLEMENTATION, PasTypes.TYPE_SECTION, PasTypes.CONST_SECTION, PasTypes.VAR_SECTION);

    private static final TokenSet TOKEN_STATEMENT_OR_DECL = TokenSet.create(PasTypes.STATEMENT,
            PasTypes.INTERFACE, PasTypes.IMPLEMENTATION, PasTypes.MODULE,
            PasTypes.BLOCK_LOCAL, PasTypes.TYPE_DECL);

    public static final TokenSet DECL_SECTIONS = TokenSet.create(
            PasTypes.TYPE_DECLARATION, PasTypes.VAR_DECLARATION, PasTypes.CONST_DECLARATION
    );

    private static final TokenSet TOKENS_IN_STRUCT_LF_AFTER = TokenSet.create(
            PasTypes.VISIBILITY, PasTypes.CLASS_PARENT, PasTypes.RECORD,
            PasTypes.CLASS_PROPERTY, PasTypes.CLASS_METHOD_RESOLUTION,
            PasTypes.TYPE_DECLARATION, PasTypes.VAR_DECLARATION, PasTypes.CONST_DECLARATION);

    public static final TokenSet ROUTINE_IMPLS = TokenSet.create(
            PasTypes.ROUTINE_IMPL_DECL, PasTypes.ROUTINE_IMPL_DECL_NESTED_1, PasTypes.ROUTINE_IMPL_DECL_WO_NESTED
    );

    private static final Map<IElementType, IElementType> TOKEN_UNINDENTED_MAP = getTokenUnindentedMap();

    private static Map<IElementType, IElementType> getTokenUnindentedMap() {
        Map<IElementType, IElementType> result = new HashMap<IElementType, IElementType>();
//        result.put(PasTypes.STATEMENT, PasTypes.COMPOUND_STATEMENT);
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
        if (ATOMIC_TOKENS.contains(myNode.getElementType())) {
            return Collections.emptyList();
        }
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

        return new PascalBlock(childNode, mySettings, myWrap, indent);
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
        CommonCodeStyleSettings commonSettings = mySettings.getCommonSettings(PascalLanguage.INSTANCE);
        final boolean keepBreaks = commonSettings.KEEP_LINE_BREAKS;
        final int spSemiA = commonSettings.SPACE_AFTER_SEMICOLON ? 1 : 0;
        int spMax = blockIs(child2, PasTypes.COMMENT) ? Integer.MAX_VALUE : spSemiA;
        if (myNode.getElementType() == PasTypes.COMPOUND_STATEMENT) {
            boolean singleLine = isSimpleStatement(myNode) &&
                   (   commonSettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE && nodeIs(myNode.getTreeParent(), PasTypes.STATEMENT)
                    || commonSettings.KEEP_SIMPLE_METHODS_IN_ONE_LINE && nodeIs(myNode.getTreeParent(), PasTypes.BLOCK_BODY));
            if (blockIs(child1, PasTypes.BEGIN) || blockIs(child2, PasTypes.END)) {
                return Spacing.createSpacing(1, 1, singleLine ? 0 : 1, keepBreaks,
                        blockIs(child2, PasTypes.END) ? commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE : commonSettings.KEEP_BLANK_LINES_IN_CODE);
            }
//            System.out.println("getSpacing: " + block2Str(child1) + ", " + block2Str(child2));
        }
        if (blockIs(child1, PasTypes.SEMI) && blockIs(child2, PasTypes.STATEMENT)) {
            final int keepLines = child2.getTextRange().getLength() > 0 ? commonSettings.KEEP_BLANK_LINES_IN_CODE : commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE;
            return Spacing.createSpacing(spSemiA, spSemiA,
                    commonSettings.KEEP_MULTIPLE_EXPRESSIONS_IN_ONE_LINE ? 0 : 1, keepBreaks, keepLines);
        } else if (blockIs(child1, PasTypes.SEMI)) {
            if (blockIs(child2, PasTypes.CLASS_FIELD)) {
                return Spacing.createSpacing(spSemiA, spSemiA, commonSettings.BLANK_LINES_AROUND_FIELD + 1,
                        keepBreaks, commonSettings.KEEP_BLANK_LINES_IN_DECLARATIONS);
            } else if (nodeIs(myNode.getTreeParent(), PasTypes.TYPE_DECL)) {
                return Spacing.createSpacing(spSemiA, spSemiA, 1, keepBreaks, commonSettings.KEEP_BLANK_LINES_IN_DECLARATIONS);
            } else {
                return Spacing.createSpacing(spSemiA, spMax, 0, keepBreaks, getKeepLines(myNode));
            }
        } else if (blockIs(child2, PasTypes.COMMENT)) {
            return Spacing.createSpacing(1, spMax, 0, keepBreaks, commonSettings.KEEP_BLANK_LINES_IN_DECLARATIONS);
        } else if (blockIs(child1, TOKENS_IN_STRUCT_LF_AFTER) && nodeIs(myNode.getTreeParent(), PasTypes.TYPE_DECL)) {
            return Spacing.createSpacing(spSemiA, spMax, commonSettings.BLANK_LINES_AROUND_FIELD + 1,
                    keepBreaks, commonSettings.KEEP_BLANK_LINES_IN_DECLARATIONS);
        } else if (blockIs(child2, PasTypes.END)) {
            return Spacing.createSpacing(1, 1, 1, keepBreaks, commonSettings.KEEP_BLANK_LINES_IN_DECLARATIONS);
        } else if (blockIs(child2, PasTypes.UNIT_INTERFACE) || blockIs(child2, PasTypes.UNIT_IMPLEMENTATION)) {
            return Spacing.createSpacing(1, 1, commonSettings.BLANK_LINES_AFTER_PACKAGE + 1,
                    keepBreaks, commonSettings.KEEP_BLANK_LINES_IN_DECLARATIONS);
        }
        return mySpacingBuilder.getSpacing(this, child1, child2);
    }

    private static boolean nodeIs(ASTNode node, TokenSet tokenSet) {
        return node != null && tokenSet.contains(node.getElementType());
    }

    private static boolean blockIs(Block block, TokenSet tokenSet) {
        if (!(block instanceof PascalBlock)) {
            return false;
        }
        return nodeIs(((PascalBlock) block).myNode, tokenSet);
    }

    private int getKeepLines(ASTNode node) {
        ASTNode parent = TreeUtil.findParent(node, TOKEN_STATEMENT_OR_DECL);
        CommonCodeStyleSettings commonSettings = mySettings.getCommonSettings(PascalLanguage.INSTANCE);
        return nodeIs(parent, PasTypes.STATEMENT) ? commonSettings.KEEP_BLANK_LINES_IN_CODE : commonSettings.KEEP_BLANK_LINES_IN_DECLARATIONS;
    }

    private static boolean isSimpleStatement(ASTNode node) {
        return node.getChildren(TokenSet.create(PasTypes.STATEMENT)).length <= 2;
    }

    @Nullable
    @Override
    public Wrap getWrap() {
        CommonCodeStyleSettings commonSettings = mySettings.getCommonSettings(PascalLanguage.INSTANCE);
        final PascalCodeStyleSettings pascalSettings = mySettings.getCustomSettings(PascalCodeStyleSettings.class);
        if (nodeIs(myNode, PasTypes.COMPOUND_STATEMENT)) {
            if ((!nodeIs(myNode.getTreeParent(), PasTypes.STATEMENT) && !(isSimpleStatement(myNode) && commonSettings.KEEP_SIMPLE_METHODS_IN_ONE_LINE))
                || (pascalSettings.BEGIN_ON_NEW_LINE && nodeIs(myNode.getTreeParent(), PasTypes.STATEMENT)
                    && (!nodeIs(myNode.getTreeParent().getTreeParent(), PasTypes.IF_ELSE_STATEMENT)))
                    ) {
                return WRAP_ALWAYS;
            }
        } else if (nodeIs(myNode, PasTypes.VISIBILITY)) {
            return WRAP_ALWAYS;
        } else if (nodeIs(myNode, PascalLexer.DECL_SECTION_KEY) && nodeIs(myNode.getTreeParent(), DECL_SECTIONS)) {
            return WRAP_ALWAYS;
        }
        return super.getWrap();
    }

    private static boolean blockIs(Block block, IElementType type) {
        if (!(block instanceof PascalBlock)) {
            return false;
        }
        return nodeIs(((PascalBlock) block).myNode, type);
    }

    private static boolean nodeIs(ASTNode node, IElementType type) {
        return node != null && node.getElementType() == type;
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
//            System.out.println("!Enter ind: " + childNode.getTreeParent() + " . " + childNode +
//                    " | " + FormatterUtil.getPreviousNonWhitespaceSibling(childNode) + " | " + FormatterUtil.getPreviousNonWhitespaceLeaf(childNode));
            if ((TOKENS_ENTER_INDENTED.contains(childNode.getElementType())) || (nodeIs(childNode.getTreeParent(), PasTypes.STATEMENT))) {
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

    private Indent getBlockIndent(@Nullable ASTNode childNode) {
        if (childNode != null) {
            if (TOKENS_COMMENT.contains(childNode.getElementType())) {
                // Not move at leftmost position, indent usually, not indent in already indented contexts such as statement
                Indent commentIndent = Indent.getAbsoluteNoneIndent();
                int curInd = getCurrentIndent(childNode);
                if (curInd > 0) {
                    if (myNode.getElementType() == PasTypes.ENUM_TYPE) {
                        commentIndent = Indent.getNormalIndent(true);
                    } else if (TOKEN_COMMENT_NORMALINDENT.contains(myNode.getElementType())) {
                        commentIndent = Indent.getNormalIndent(false);
                    } else {
                        commentIndent = Indent.getContinuationIndent();
                    }
                }
                return commentIndent;
            }
        }

        if (nodeIs(childNode, TOKENS_INDENT_BLOCK)) {
            return Indent.getNormalIndent(true);
        } else if (TOKENS_INDENT_CHILD_NON_RELATIVE.contains(myNode.getElementType()) && (childNode != null)) {
            final PascalCodeStyleSettings pascalSettings = mySettings.getCustomSettings(PascalCodeStyleSettings.class);
            if (!nodeIs(myNode, PasTypes.STATEMENT) || !nodeIs(childNode, PasTypes.COMPOUND_STATEMENT)) {
                return Indent.getNormalIndent();
            }
        } else if (shouldIndentCompound(myNode)) {
            return Indent.getNormalIndent();
        } else if (TOKENS_INDENT_CHILD.contains(myNode.getElementType()) && (childNode != null)) {
            if (TOKEN_UNINDENTED_MAP.get(myNode.getElementType()) != childNode.getElementType()) {
                return Indent.getNormalIndent(true);
            }
        }
        return Indent.getNoneIndent();
    }

    private boolean shouldIndentCompound(ASTNode myNode) {
        final PascalCodeStyleSettings pascalSettings = mySettings.getCustomSettings(PascalCodeStyleSettings.class);
        return myNode.getElementType() == PasTypes.COMPOUND_STATEMENT && pascalSettings.INDENT_BEGIN_END
                && nodeIsNotBlock(myNode.getTreeParent());
    }

    private boolean nodeIsNotBlock(ASTNode node) {
        return (node != null) && (node.getElementType() != PasTypes.UNIT_IMPLEMENTATION) && (node.getElementType() != PasTypes.BLOCK_BODY);
    }

    private static int getCurrentIndent(ASTNode childNode) {
        ASTNode prev = childNode.getTreePrev();
        if (prev instanceof PsiWhiteSpace) {
            String text = prev.getText();
            int pos = Math.min(text.length(), text.length() - text.lastIndexOf('\n') - 1);
            return pos;
        }
        return 0;
    }

}
