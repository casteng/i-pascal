package com.siberika.idea.pascal.editor.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 02/10/2013
 */
public class PascalBlock extends AbstractBlock implements Block {
    public PascalBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment) {
        super(node, wrap, alignment);
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> result = new ArrayList<Block>();
        /*PsiElement psi = myNode.getPsi();
        Collection<PasUsesFileClause> uses = PsiTreeUtil.findChildrenOfAnyType(psi, PasUsesFileClause.class);
        for (PasUsesFileClause usesFileClause : uses) {
            if (!usesFileClause.getNamespaceIdentList().isEmpty()) {
                Block block = new PascalBlock(usesFileClause.getNode(), myWrap, myAlignment);
                if (result.isEmpty() && (block.getTextRange().getStartOffset() > 0)) {
                    result.add(new PascalBlock())
                }
                result.add(block);
            }
        }*/
        return result;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        System.out.println("getSpacing: " + block2Str(child1) + ", " + block2Str(child2));
        return Spacing.getReadOnlySpacing();
    }

    private String block2Str(Block r) {
        if (null == r) {
            return "[-]";
        }
        return "[" + r.getTextRange().getStartOffset() + ".." + r.getTextRange().getEndOffset() + "]";
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
