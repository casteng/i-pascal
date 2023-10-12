package com.siberika.idea.pascal.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Author: George Bakhtadze
 * Date: 16/09/2013
 */
public class PasStructureViewTreeElement extends PsiTreeElementBase<PsiElement> implements StructureViewTreeElement {
    private final PasField field;
    protected PasStructureViewTreeElement(PsiElement psiElement, PasField field) {
        super(field != null ? field.getElement() : psiElement);
        this.field = field;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return Collections.emptyList();
    }

    @NotNull
    public static Collection<StructureViewTreeElement> collectChildren(@Nullable PasEntityScope element) {
        if (null == element) {
            return Collections.emptyList();
        }
        Collection<PasField> fields = element.getAllFields();
        Collection<StructureViewTreeElement> res = new ArrayList<StructureViewTreeElement>(fields.size());
        for (PasField field : fields) {
            if (PasField.TYPES_STRUCTURE.contains(field.fieldType)) {
                if (field.fieldType == PasField.FieldType.TYPE) {
                    PascalStructType structType = PasStructStructureTreeElement.getStructElement(field);
                    if (structType != null) {
                        res.add(new PasStructStructureTreeElement(structType));
                    } else {
                        res.add(new PasStructureViewTreeElement(field.getElement(), field));
                    }
                } else {
                    res.add(new PasStructureViewTreeElement(field.getElement(), field));
                }
            }
        }
        return res;
    }

    public PasField getField() {
        return field;
    }

    @Override
    public Icon getIcon(boolean open) {
        if (field != null) {
            if (field.fieldType == PasField.FieldType.VARIABLE) {
                return PascalIcons.VARIABLE;
            } else if (field.fieldType == PasField.FieldType.CONSTANT) {
                return PascalIcons.CONSTANT;
            } else if (field.fieldType == PasField.FieldType.TYPE) {
                return PascalIcons.TYPE;
            } else if (field.fieldType == PasField.FieldType.PROPERTY) {
                return PascalIcons.PROPERTY;
            } else if (field.fieldType == PasField.FieldType.ROUTINE) {
                return PascalIcons.ROUTINE;
            }
        }
        return super.getIcon(open);
    }

    @Nullable
    @Override
    public String getPresentableText() {
        PsiElement element = getElement();
        if (element instanceof PascalNamedElement) {
            return PsiUtil.getFieldName((PascalNamedElement) element);
        } else if (element instanceof PsiFile) {
            return ((PsiFile) element).getName();
        } else {
            return "-";
        }
    }

}
