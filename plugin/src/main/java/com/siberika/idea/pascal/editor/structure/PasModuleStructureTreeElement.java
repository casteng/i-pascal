package com.siberika.idea.pascal.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasModule;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 06/05/2015
 */
public class PasModuleStructureTreeElement extends PsiTreeElementBase<PasModule> {
    protected PasModuleStructureTreeElement(PasModule psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return PasStructureViewTreeElement.collectChildren(getElement());
    }

    @Override
    public Icon getIcon(boolean open) {
        if (getElement() != null) {
            switch (getElement().getModuleType()) {
                case PACKAGE:
                case LIBRARY:
                    return PascalIcons.FILE_LIBRARY;
                case PROGRAM:
                    return PascalIcons.FILE_PROGRAM;
                case UNIT:
                    return PascalIcons.UNIT;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        String name = getElement() != null ? getElement().getName() : "-";
        if (StringUtils.isEmpty(name)) {
            name = getElement().getContainingFile().getName();
        }
        return name;
    }
}
