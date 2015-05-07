package com.siberika.idea.pascal.editor.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.PsiFile;

/**
 * Author: George Bakhtadze
 * Date: 07/05/2015
 */
public class PascalStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
    public PascalStructureViewModel(PsiFile psiFile, StructureViewTreeElement root) {
        super(psiFile, root);
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element instanceof PasStructureViewTreeElement;
    }
}
