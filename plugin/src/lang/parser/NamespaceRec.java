package com.siberika.idea.pascal.lang.parser;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;

import java.util.ArrayList;
import java.util.List;

/**
* Author: George Bakhtadze
* Date: 12/08/2013
*/
public class NamespaceRec {
    private final List<PasSubIdent> levels;
    private final PasQualifiedIdent parentIdent;
    private final int target;
    private int current;

    public NamespaceRec(PasSubIdent subIdent) {
        int targetInd = -1;
        levels = new ArrayList<PasSubIdent>();
        if (subIdent.getParent() instanceof PasQualifiedIdent) {
            parentIdent = (PasQualifiedIdent) subIdent.getParent();
            for (PsiElement subEl : parentIdent.getChildren()) {
                if (subEl instanceof PasSubIdent) {
                    if (subEl == subIdent) {
                        targetInd = levels.size();
                    }
                    levels.add((PasSubIdent) subEl);
                }
            }
            if (-1 == targetInd) {
                targetInd = levels.size() - 1;
            }
        } else {
            parentIdent = null;
        }
        target = targetInd;
        current = 0;
    }

    public PasSubIdent getCurrent() {
        return levels.get(current);
    }

    public void next() {
        current++;
    }

    public void prev() {
        current--;
    }

    public boolean isEmpty() {
        return levels.size() == 0;
    }

    public boolean isLast() {
        return current >= levels.size()-1;
    }

    public boolean isTarget() {
        return current == target;
    }

    public PasQualifiedIdent getParentIdent() {
        return parentIdent;
    }

}
