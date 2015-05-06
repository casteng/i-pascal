package com.siberika.idea.pascal.editor.structure;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 16/09/2013
 */
public class PasStructureViewFactory implements PsiStructureViewFactory {
    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                StructureViewTreeElement te;
                PasModule mod = PsiUtil.getElementPasModule(psiFile);
                if (mod != null) {
                    te = new PasModuleStructureTreeElement(mod);
                } else {
                    te = new PasStructureViewTreeElement(psiFile, null);
                }
                return new StructureViewModelBase(psiFile.getContainingFile(), te);
            }
        };
    }
}
