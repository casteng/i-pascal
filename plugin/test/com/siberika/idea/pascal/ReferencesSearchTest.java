package com.siberika.idea.pascal;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Query;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.DocUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReferencesSearchTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/reference";
    }

    public void testFromOtherUnit() {
        myFixture.configureByFiles("unit1.pas", "unit2.pas");
        List<PasEntityScope> decls = getDeclarations("unit1");
        System.out.println("===*** decls: " + decls.size());
        SearchScope scope = GlobalSearchScope.allScope(getProject());
        for (PasEntityScope decl : decls) {
            Document doc = myFixture.getDocument(decl.getContainingFile());
            System.out.println(String.format("Decl: %s (%s)", DocUtil.getWholeLineAt(doc, decl.getTextRange().getStartOffset()), DocUtil.getLocation(doc, decl)));
            Collection<PsiReference> refs = new ArrayList<>();
            Query<PsiReference> query = ReferencesSearch.search(decl, scope);
            query.forEach(new CommonProcessors.CollectProcessor<>(refs));
            for (PsiReference ref : refs) {
                PsiElement el = ref.getElement();
                doc = myFixture.getDocument(el.getContainingFile());
                System.out.println(String.format("  ref: %s (%s)", DocUtil.getWholeLineAt(doc, el.getTextRange().getStartOffset()), DocUtil.getLocation(doc, el)));
            }

        }
    }

    private List<PasEntityScope> getDeclarations(String unitName) {
        List<PasEntityScope> res = new ArrayList<>();
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), unitName);
        Collection<PasEntityScope> scopes = PsiTreeUtil.findChildrenOfType(mod, PasEntityScope.class);
        for (PasEntityScope scope : scopes) {
            if ((scope instanceof PascalRoutine)) {
                res.add(scope);
            }
        }
        return res;
    }

}
