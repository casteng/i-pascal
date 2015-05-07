package com.siberika.idea.pascal.editor.structure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 06/05/2015
 */
public class PasStructStructureTreeElement extends PsiTreeElementBase<PascalStructType> {
    protected PasStructStructureTreeElement(PascalStructType psiElement) {
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
            PasTypeDecl typeDecl = PsiUtil.getTypeDeclaration(getElement());
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
        }
        return null;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return getElement() != null ? getElement().getName() : "-";
    }

    public static PascalStructType getStructElement(PasField field) {
        PasTypeDecl typeDecl = PsiUtil.getTypeDeclaration(field.element);
        if (typeDecl != null) {
            if (typeDecl.getInterfaceTypeDecl() != null) {
                return typeDecl.getInterfaceTypeDecl();
            } if (typeDecl.getClassTypeDecl() != null) {
                return typeDecl.getClassTypeDecl();
            } if (typeDecl.getObjectDecl() != null) {
                return typeDecl.getObjectDecl();
            } if (typeDecl.getRecordDecl() != null) {
                return typeDecl.getRecordDecl();
            } if (typeDecl.getClassHelperDecl() != null) {
                return typeDecl.getClassHelperDecl();
            } if (typeDecl.getRecordHelperDecl() != null) {
                return typeDecl.getRecordHelperDecl();
            }
        }
        return null;
    }
}
