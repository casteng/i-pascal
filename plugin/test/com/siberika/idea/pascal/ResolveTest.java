package com.siberika.idea.pascal;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class ResolveTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/misc";
    }

    public void testGetDefaultProperty() throws Exception {
        myFixture.configureByFiles("routinesForward.pas");
        PsiReference ref = myFixture.getFile().findReferenceAt(140);
        PsiElement decl = ref.resolve();
        assertEquals(89, decl.getTextRange().getStartOffset());
    }

}
