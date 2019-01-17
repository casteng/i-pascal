package com.siberika.idea.pascal.lang.search;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.ExecutorsQuery;
import com.intellij.util.Query;
import com.siberika.idea.pascal.ide.actions.PascalDefinitionsSearch;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;

import java.util.Collections;

public class DescendingEntities {
    public static Query<PasEntityScope> getQuery(PsiElement entity, SearchScope scope) {
        return new ExecutorsQuery<PasEntityScope, DefinitionsScopedSearch.SearchParameters>(new DefinitionsScopedSearch.SearchParameters(entity, scope, false),
                Collections.singletonList(new PascalDefinitionsSearch()));
    }

}
