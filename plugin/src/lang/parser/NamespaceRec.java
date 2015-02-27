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

    private NamespaceRec(PsiElement context) {
        levels = Collections.emptyList();
        parentIdent = context;
        target = 0;
        current = 0;
    }

    private NamespaceRec(PasSubIdent subIdent) {
        this(getParent(subIdent), subIdent);
    }

    private NamespaceRec(PasRefNamedIdent element) {
        levels = new ArrayList<PascalNamedElement>(1);
        parentIdent = element.getParent();
        levels.add(element);
        target = 0;
        current = 0;
    }

    private static PascalQualifiedIdent getParent(PasSubIdent subIdent) {
        return subIdent.getParent() instanceof PascalQualifiedIdent ? (PascalQualifiedIdent) subIdent.getParent() : null;
    }

    /**
     * Creates instance from qualified ident with specified target subident
     */
    private NamespaceRec(PascalQualifiedIdent qualifiedIdent, PasSubIdent targetIdent) {
        assert (targetIdent == null) || (targetIdent.getParent() == qualifiedIdent);
        int targetInd = -1;
        levels = new ArrayList<PascalNamedElement>();
        parentIdent = qualifiedIdent;
        for (PasSubIdent subEl : qualifiedIdent.getSubIdentList()) {
            if (subEl == targetIdent) {
                targetInd = levels.size();
            }
            levels.add(subEl);
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

    public boolean isTarget() {
        return current == target;
    }

    public PsiElement getParentIdent() {
        return parentIdent;
    }

    public static NamespaceRec fromElement(PsiElement element) {
        if (element instanceof PasSubIdent) {
            return new NamespaceRec((PascalQualifiedIdent) element.getParent(), (PasSubIdent) element);
        } else if (element instanceof PascalQualifiedIdent) {
            return new NamespaceRec((PascalQualifiedIdent) element, null);
        } else if (element instanceof PasRefNamedIdent) {
            return new NamespaceRec((PasRefNamedIdent) element);
        }
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
