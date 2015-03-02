package com.siberika.idea.pascal.lang.parser;

import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
* Author: George Bakhtadze
* Date: 12/08/2013
*/
public class NamespaceRec {
    private final List<String> levels;
    private final PsiElement parentIdent;
    private final int target;
    private int current;

    private NamespaceRec(PsiElement context) {
        assert context != null;
        levels = Collections.emptyList();
        parentIdent = context;
        target = 0;
        current = 0;
    }

    private NamespaceRec(@NotNull PasSubIdent subIdent) {
        this(getParent(subIdent), subIdent);
    }

    private NamespaceRec(@NotNull PasRefNamedIdent element) {
        levels = new SmartList<String>();
        parentIdent = element.getParent() != null ? element.getParent() : element;
        levels.add(element.getName());
        target = 0;
        current = 0;
    }

    private static PascalQualifiedIdent getParent(PasSubIdent subIdent) {
        return subIdent.getParent() instanceof PascalQualifiedIdent ? (PascalQualifiedIdent) subIdent.getParent() : null;
    }

    /**
     * Creates instance from qualified ident with specified target subident
     */
    private NamespaceRec(@NotNull PascalQualifiedIdent qualifiedIdent, @Nullable PasSubIdent targetIdent) {
        assert (targetIdent == null) || (targetIdent.getParent() == qualifiedIdent);
        int targetInd = -1;
        levels = new SmartList<String>();
        parentIdent = qualifiedIdent;
        for (PasSubIdent subEl : qualifiedIdent.getSubIdentList()) {
            if (subEl == targetIdent) {
                targetInd = levels.size();
            }
            levels.add(subEl.getName());
        }
        if (-1 == targetInd) {
            targetInd = levels.size() - 1;
        }
        target = targetInd;
        current = 0;
    }

    /*public PascalNamedElement getCurrent() {
        return levels.get(current);
    }*/

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

    @NotNull
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
        return current < levels.size() ? levels.get(current) : null;
    }

}
