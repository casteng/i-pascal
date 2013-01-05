package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.PascalLanguage;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public class PasElementFactory {
    private PasElementFactory() {
    }

    public static PsiElement createLeafFromText(Project project, String text) {
        PsiFile fileFromText = PsiFileFactory.getInstance(project).createFileFromText("_.pas", PascalLanguage.INSTANCE, text);
        return PsiTreeUtil.getDeepestFirst(fileFromText);
    }

}
