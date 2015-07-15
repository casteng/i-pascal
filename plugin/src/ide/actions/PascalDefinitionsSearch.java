package com.siberika.idea.pascal.ide.actions;

import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;

/**
 * Author: George Bakhtadze
 * Date: 02/07/2015
 */
public class PascalDefinitionsSearch implements QueryExecutor<PsiElement, PsiElement> {
    @Override
    public boolean execute(PsiElement queryParameters, Processor<PsiElement> consumer) {
        System.out.println("===***");
        return false;
    }
}
