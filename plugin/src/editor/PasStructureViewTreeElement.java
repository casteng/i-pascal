package com.siberika.idea.pascal.editor;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Author: George Bakhtadze
 * Date: 16/09/2013
 */
public class PasStructureViewTreeElement extends PsiTreeElementBase<PsiElement> implements StructureViewTreeElement {
    protected PasStructureViewTreeElement(PsiElement psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
