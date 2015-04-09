package com.siberika.idea.pascal;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GotoSymbolTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/gotoSymbol";
    }

    public void testFindSymbol() {
        myFixture.configureByFiles("gotoSymbolTest.pas");
        Collection<PascalNamedElement> symbols = PascalParserUtil.findSymbols(myFixture.getProject(), "");
        Set<String> names = new HashSet<String>(symbols.size());
        for (PascalNamedElement symbol : symbols) {
            names.add(symbol.getName());
        }
        assertEquals(new HashSet<String>(Arrays.asList("a", "b", "c")), names);
    }
}
