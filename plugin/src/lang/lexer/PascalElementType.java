package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalElementType extends IElementType {

  private String debugName = null;

  public PascalElementType(String debugName) {
    super(debugName, PascalLanguage.INSTANCE);
    this.debugName = debugName;
  }

  public String toString() {
    return debugName;
  }

  public static abstract class PsiCreator extends PascalElementType {
    protected PsiCreator(String debugName) {
      super(debugName);
    }

    public abstract PascalPsiElement createPsi(@NotNull ASTNode node);
  }

}
