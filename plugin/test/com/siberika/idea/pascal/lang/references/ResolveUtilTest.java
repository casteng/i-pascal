package com.siberika.idea.pascal.lang.references;

import com.google.common.collect.ImmutableMap;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.junit.Assert;

import java.util.Collection;
import java.util.Map;

public class ResolveUtilTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "testData/resolve";
    }

    public void testGetDeclarationTypeString() {
        ImmutableMap.Builder<String, String> b = ImmutableMap.builder();
        Map<String, String> exp = b
                .put("TB", "TB")
                .put("TA", "TB")
                .put("TC", "TA")
                .put("CA", "TA")
                .put("A", "TA")
                .put("B", "TB")
                .put("AA", "TB")
                .put("PropA", "TA")
                .put("func", "TR")
                .build();
        myFixture.configureByFiles("declarationTypes.pas");
        Collection<PascalNamedElement> decls = PsiTreeUtil.findChildrenOfType(myFixture.getFile(), PasNamedIdent.class);
        for (PascalNamedElement decl : decls) {
            System.out.println(String.format("%s: %s", decl.getName(), ResolveUtil.getDeclarationTypeString(decl)));
        }

        for (PascalNamedElement decl : decls) {
            Assert.assertEquals(exp.get(decl.getName()), ResolveUtil.getDeclarationTypeString(decl));
        }
    }
}