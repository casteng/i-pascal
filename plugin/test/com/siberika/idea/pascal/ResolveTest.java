package com.siberika.idea.pascal;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class ResolveTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/misc";
    }

    public void testResolveForwardRoutine() throws Exception {
        myFixture.configureByFiles("routinesFwd.pas");
        PsiReference ref = myFixture.getFile().findReferenceAt(140);
        PsiElement decl = ref.resolve();
        assertEquals(85, decl.getTextRange().getStartOffset());
    }

    public void testResolveInherited() throws Exception {
        myFixture.configureByFiles("resolveInherited.pas");
        PsiReference ref = myFixture.getFile().findReferenceAt(310);
        PsiElement decl = ref.resolve();
        assertEquals(68, decl.getTextRange().getStartOffset());
    }

}
