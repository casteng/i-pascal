package com.siberika.idea.pascal.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasClassHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasObjectDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordHelperDecl;
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
            PascalStructType el = getElement();
            if (el instanceof PasInterfaceTypeDecl) {
                return PascalIcons.INTERFACE;
            } else if (el instanceof PasClassTypeDecl) {
                return PascalIcons.CLASS;
            } else if (el instanceof PasObjectDecl) {
                return PascalIcons.OBJECT;
            } else if (el instanceof PasRecordDecl) {
                return PascalIcons.RECORD;
            } else if ((el instanceof PasClassHelperDecl) || (el instanceof PasRecordHelperDecl)) {
                return PascalIcons.HELPER;
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
