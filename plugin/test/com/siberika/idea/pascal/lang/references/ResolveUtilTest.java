package com.siberika.idea.pascal.lang.references;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.junit.Assert;

import java.util.Collection;
import java.util.Map;

import static com.siberika.idea.pascal.lang.psi.impl.PasField.Kind.ARRAY;
import static com.siberika.idea.pascal.lang.psi.impl.PasField.Kind.CLASSREF;
import static com.siberika.idea.pascal.lang.psi.impl.PasField.Kind.POINTER;
import static com.siberika.idea.pascal.lang.psi.impl.PasField.Kind.STRUCT;
import static com.siberika.idea.pascal.lang.psi.impl.PasField.Kind.TYPEREF;

public class ResolveUtilTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "testData/resolve";
    }

    public void testGetDeclarationTypeString() {
        Map<String, Pair<String, PasField.Kind>> exp = ImmutableMap.<String, Pair<String, PasField.Kind>>builder()
                .put("TB", Pair.create("TB", TYPEREF))
                .put("TA", Pair.create("TB", TYPEREF))
                .put("TC", Pair.create("TA", CLASSREF))
                .put("TR", Pair.create(null, STRUCT))
                .put("x", Pair.create("Integer", TYPEREF))
                .put("CA", Pair.create("TA", TYPEREF))
                .put("A", Pair.create("TA", TYPEREF))
                .put("B", Pair.create("TB", TYPEREF))
                .put("PB", Pair.create("^TB", POINTER))
                .put("AB", Pair.create("TB", ARRAY))
                .put("R", Pair.create(null, STRUCT))
                .put("y", Pair.create("integer", TYPEREF))
                .put("AAA", Pair.create("TA", ARRAY))
                .put("AOR", Pair.create(null, STRUCT))
                .put("PropA", Pair.create("TA", TYPEREF))
                .put("func", Pair.create("TR", TYPEREF))
                .build();
        myFixture.configureByFiles("declarationTypes.pas");
        Collection<PascalNamedElement> decls = PsiTreeUtil.findChildrenOfType(myFixture.getFile(), PasNamedIdent.class);
        for (PascalNamedElement decl : decls) {
            System.out.println(String.format("%s: %s", decl.getName(), ResolveUtil.retrieveDeclarationType(decl)));
        }

        for (PascalNamedElement decl : decls) {
            Assert.assertEquals(exp.get(decl.getName()), ResolveUtil.retrieveDeclarationType(decl));
        }
    }
}