package com.siberika.idea.pascal.editor.structure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

/**
 * Author: George Bakhtadze
 * Date: 16/09/2013
 */
public class PasStructureViewTreeElement extends PsiTreeElementBase<PsiElement> implements StructureViewTreeElement {
    private final PasField field;
    protected PasStructureViewTreeElement(PsiElement psiElement, PasField field) {
        super(field != null ? field.element : psiElement);
        this.field = field;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return Collections.emptyList();
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
                PasTypeDecl typeDecl = PsiUtil.getTypeDeclaration(field.element);
                if (typeDecl != null) {
                    if (typeDecl.getInterfaceTypeDecl() != null) {
                        return AllIcons.Nodes.Interface;//PascalIcons.INTERFACE;
                    } if (typeDecl.getClassTypeDecl() != null) {
                        return AllIcons.Nodes.Class;// PascalIcons.CLASS;
                    } if (typeDecl.getObjectDecl() != null) {
                        return PascalIcons.OBJECT;
                    } if (typeDecl.getRecordDecl() != null) {
                        return PascalIcons.RECORD;
                    } if ((typeDecl.getClassHelperDecl() != null) || (typeDecl.getRecordHelperDecl() != null)) {
                        return PascalIcons.HELPER;
                    }
                }
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
        if (element instanceof PascalNamedElement){
            return ((PascalNamedElement) element).getName();
        } else if (element instanceof PsiFile) {
            return ((PsiFile) element).getName();
        } else {
            return "-";
        }
    }
}
