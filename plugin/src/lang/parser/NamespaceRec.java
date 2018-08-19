package com.siberika.idea.pascal.lang.parser;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
* Author: George Bakhtadze
* Date: 12/08/2013
*/
public class NamespaceRec {
    private String[] levels;
    private final PsiElement parentIdent;
    private int target;
    private int current;

    private static final String[] EMPTY_LEVELS = {};
    private boolean nested = false;
    private boolean ignoreVisibility = false;

    // Levels should be w/o "&"
    private NamespaceRec(@NotNull String[] levels, @NotNull PsiElement parentIdent, int target) {
        this.levels = levels;
        this.parentIdent = parentIdent;
        this.target = target;
        this.current = 0;
    }

    private NamespaceRec(@NotNull PsiElement context) {
        this(EMPTY_LEVELS, context, 0);
    }

    private NamespaceRec(@NotNull PasSubIdent subIdent) {
        this(getParent(subIdent), subIdent);
    }

    private NamespaceRec(@NotNull PasRefNamedIdent element) {
        this(new String[] {!element.getName().startsWith("&") ? element.getName() : element.getName().substring(1)},
                element.getParent() != null ? element.getParent() : element, 0);
    }

    public void addPrefix(NamespaceRec original, String prefix) {
        String[] lvls = prefix.split("\\.", 100);
        String[] newLevels = new String[lvls.length + original.levels.length];
        int ind = 0;
        for (String lvl : lvls) {
            newLevels[ind++] = lvl;
        }
        for (String lvl : original.levels) {
            newLevels[ind++] = lvl;
        }
        levels = newLevels;
        target = original.target + lvls.length;
    }

    /**
     * Creates instance from qualified ident with specified target subident
     */
    private NamespaceRec(@NotNull PascalQualifiedIdent qualifiedIdent, @Nullable PasSubIdent targetIdent) {
        assert (targetIdent == null) || (targetIdent.getParent() == qualifiedIdent);
        int targetInd = -1;
        List<PasSubIdent> idents = qualifiedIdent.getSubIdentList();
        levels = new String[idents.size()];
        parentIdent = qualifiedIdent;
        for (int i = 0; i < idents.size(); i++) {
            if (idents.get(i) == targetIdent) {
                targetInd = i;
            }
            levels[i] = idents.get(i).getName();//.replace(PasField.DUMMY_IDENTIFIER, ""));
        }
        if (-1 == targetInd) {
            targetInd = levels.length - 1;
        }
        target = targetInd;
        current = 0;
    }

    public NamespaceRec(NamespaceRec fqn) {
        this.levels = fqn.levels;
        this.parentIdent = fqn.parentIdent;
        this.target = fqn.target;
        this.current = fqn.current;
        this.nested = fqn.nested;
        this.ignoreVisibility = fqn.ignoreVisibility;
    }

    private static PascalQualifiedIdent getParent(PasSubIdent subIdent) {
        return subIdent.getParent() instanceof PascalQualifiedIdent ? (PascalQualifiedIdent) subIdent.getParent() : null;
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
        return levels.length == 0;
    }

    public boolean isFirst() {
        return !nested && (current == 0);
    }

    public int getRestLevels() {
        return target - current;
    }

    public boolean isTarget() {
        return current == target;
    }

    public boolean isBeforeTarget() {
        return current < target;
    }

    public boolean isComplete() {
        return current > target;
    }

    @NotNull
    public PsiElement getParentIdent() {
        return parentIdent;
    }

    @NotNull
    public static NamespaceRec fromElement(@NotNull PsiElement element) {
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

    public static NamespaceRec fromFQN(@NotNull PsiElement context, @NotNull String fqn) {
        String[] lvls = fqn.split("\\.", 100);
        for (int i = 0, lvlsLength = lvls.length; i < lvlsLength; i++) {
            String lvl = lvls[i];
            if (lvl.startsWith("&")) {
                lvls[i] = lvl.substring(1);
            }
        }

        /*if ((lvls.length > 0) && (lvls[lvls.length - 1].endsWith(PasField.DUMMY_IDENTIFIER))) {
            lvls[lvls.length - 1] = lvls[lvls.length - 1].replace(PasField.DUMMY_IDENTIFIER, "");
        }*/
        return new NamespaceRec(lvls, context, lvls.length-1);
    }

    public String getCurrentName() {
        return current < levels.length ? levels[current] : null;
    }

    public String getLastName() {
        return levels[levels.length-1];
    }

    public void clearTarget() {
        if ((target >= 0) && (levels.length > target)) {
            levels[target] = "";
        }
    }

    public void setNested(boolean nested) {
        this.nested = nested;
    }

    public boolean isIgnoreVisibility() {
        return ignoreVisibility;
    }

    public void setIgnoreVisibility(boolean ignoreVisibility) {
        this.ignoreVisibility = ignoreVisibility;
    }

    public boolean advance(String fqn) {
        String[] lvls = fqn.split("\\.");
        int i = current;
        while (((i - current) < lvls.length) && (current + i < levels.length)
                && (lvls[i - current].equalsIgnoreCase(levels[i]))) {
            i++;
        }
        if ((i - current) >= lvls.length) {
            current = i;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d) %s for: %s", Arrays.toString(levels), current, target, nested ? "nested" : "", parentIdent);
    }

}
