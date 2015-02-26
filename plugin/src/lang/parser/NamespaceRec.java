package com.siberika.idea.pascal.lang.parser;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* Author: George Bakhtadze
* Date: 12/08/2013
*/
public class NamespaceRec {
    private final List<PascalNamedElement> levels;
    private final PsiElement parentIdent;
    private final int target;
    private int current;

    public NamespaceRec(PsiElement context) {
        levels = Collections.emptyList();
        parentIdent = context;
        target = 0;
        current = 0;
    }

    public NamespaceRec(PasSubIdent subIdent) {
        this(getParent(subIdent), subIdent);
    }

    public NamespaceRec(PasRefNamedIdent element) {
        levels = new ArrayList<PascalNamedElement>(1);
        parentIdent = null;
        levels.add(element);
        target = 0;
        current = 0;
    }

    private static PascalQualifiedIdent getParent(PasSubIdent subIdent) {
        return subIdent.getParent() instanceof PascalQualifiedIdent ? (PascalQualifiedIdent) subIdent.getParent() : null;
    }

    public NamespaceRec(PascalQualifiedIdent qualifiedIdent, PasSubIdent subIdent) {
        assert (subIdent == null) || (subIdent.getParent() == qualifiedIdent);
        int targetInd = -1;
        levels = new ArrayList<PascalNamedElement>();
        parentIdent = qualifiedIdent;
        for (PsiElement subEl : parentIdent.getChildren()) {
            if (subEl instanceof PasSubIdent) {
                if (subEl == subIdent) {
                    targetInd = levels.size();
                }
                levels.add((PascalNamedElement) subEl);
            }
        }
        if (-1 == targetInd) {
            targetInd = levels.size() - 1;
        }
        target = targetInd;
        current = 0;
    }

    public PascalNamedElement getCurrent() {
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

    public boolean isFirst() {
        return current == 0;
    }

    public boolean isLast() {
        return current >= levels.size()-1;
    }

    public boolean isTarget() {
        return current == target;
    }

    public int getSize() {
        return levels.size();
    }

    public PsiElement getParentIdent() {
        return parentIdent;
    }

    public static NamespaceRec fromElement(PsiElement element) {
        NamespaceRec namespace;
        if (PsiUtil.isIdent(element)) {
            namespace = new NamespaceRec((PasSubIdent) element);
        } else {
            namespace = new NamespaceRec(element);
        }
        return namespace;
    }

    public String getCurrentName() {
        return current < levels.size() ? getCurrent().getName() : null;
    }
}
