package com.siberika.idea.pascal.editor.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.refactoring.rename.NameSuggestionProvider;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasVarDeclaration;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class PascalNameSuggestionProvider implements NameSuggestionProvider {
    @Nullable
    @Override
    public SuggestedNameInfo getSuggestedNames(PsiElement element, @Nullable PsiElement nameSuggestionContext, Set<String> result) {
        if (!element.getLanguage().isKindOf(PascalLanguage.INSTANCE)) {
            return null;
        }
        if (element.getParent() instanceof PasVarDeclaration) {
            suggestTypeNames(result, ((PasVarDeclaration) element.getParent()).getTypeDecl(), false);
        } else if (element.getParent() instanceof PasConstDeclaration) {
            suggestTypeNames(result, ((PasConstDeclaration) element.getParent()).getTypeDecl(), true);
        }
        return null;
    }

    private void suggestTypeNames(Set<String> result, PasTypeDecl typeDecl, boolean constant) {
        PasTypeID typeId = typeDecl != null ? typeDecl.getTypeID() : null;
        if (typeId != null) {
            String name = typeId.getFullyQualifiedIdent().getNamePart();
            if (name.startsWith("T") || name.startsWith("I") || name.startsWith("C") || name.startsWith("P")) {
                suggestNames(result, name.substring(1), constant);
            }
        }
    }

    static void suggestNames(Set<String> result, String name, boolean constant) {
        List<String> names = NameUtil.getSuggestionsByName(name, "", "", constant, true, false);
        for (String s : names) {
            if (s.length() > 1) {
                result.add(s.substring(0, 1).toUpperCase() + s.substring(1));
            }
        }
    }
}
