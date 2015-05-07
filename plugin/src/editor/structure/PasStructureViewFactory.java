package com.siberika.idea.pascal.editor.structure;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 16/09/2013
 */
public class PasStructureViewFactory implements PsiStructureViewFactory {
    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        PascalStructureViewBuilder res = new PascalStructureViewBuilder();
        res.setFile(psiFile);
        return res;
    }
}
