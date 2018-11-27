package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public class PasElementFactory {
    private PasElementFactory() {
    }

    public static PsiElement createLeafFromText(Project project, String text) {
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("$$.pas", PascalLanguage.INSTANCE, text);
        return PsiTreeUtil.getDeepestFirst(file);
    }

    public static PsiElement createElementFromText(Project project, String text) {
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("$$.pas", PascalLanguage.INSTANCE, text);
        PsiElement res = file.getFirstChild();
        if (res instanceof PsiErrorElement) {
            res = res.getNextSibling();
        }
        return res;
    }

    public static PsiElement createReplacementElement(PsiElement element, String text) {
        if (element instanceof PascalPsiElement) {
            PsiFileFactoryImpl factory = (PsiFileFactoryImpl) PsiFileFactoryImpl.getInstance(element.getProject());
            return factory.createElementFromText(text, element.getLanguage(), element.getNode().getElementType(), element);
        } else {
            return createLeafFromText(element.getProject(), text);
        }
    }
}
