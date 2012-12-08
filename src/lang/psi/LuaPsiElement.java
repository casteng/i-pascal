package com.siberika.idea.pascal.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;


public interface LuaPsiElement extends PsiElement, NavigationItem {
    LuaPsiElement[] EMPTY_ARRAY = new LuaPsiElement[0];
    
    @NotNull
    ASTNode getNode();

    void accept(LuaElementVisitor visitor);

    void acceptChildren(LuaElementVisitor visitor);

    String getPresentationText();

//    String getText();
//
//    LuaPsiElement replace(LuaPsiElement replacement);
//
//    LuaPsiElement getParent();
//
//    LuaPsiElement addBefore(LuaPsiElement replacement, LuaPsiElement original);
//
//    void delete();
}
