package com.siberika.idea.pascal.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
        if (null == getElement()) {
            return Collections.emptyList();
        }
        Collection<PasField> fields = getElement().getAllFields();
        Collection<StructureViewTreeElement> res = new ArrayList<StructureViewTreeElement>();
        for (PasField field : fields) {
            if (PasField.TYPES_STRUCTURE.contains(field.fieldType)) {
                res.add(new PasStructureViewTreeElement(null, field));
            }
        }
        return res;
    }

    @Override
    public Icon getIcon(boolean open) {
        if (getElement() != null) {
            switch (((PascalModuleImpl) getElement()).getModuleType()) {
                case PACKAGE:
                case LIBRARY:
                    return PascalIcons.MODULE;
                case PROGRAM:
                    return PascalIcons.PROGRAM;
                case UNIT:
                    return PascalIcons.UNIT;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return getElement() != null ? getElement().getName() : "-";
    }
}
