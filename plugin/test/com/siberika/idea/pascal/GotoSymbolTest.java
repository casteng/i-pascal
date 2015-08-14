package com.siberika.idea.pascal;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    public void testNormalizeRoutineName() throws Exception {
        Map<String, String> names = new HashMap<String, String>();
        names.put("test1", "test1()");
        names.put("test2", "test2()");
        names.put("test3", "test3(" + PsiUtil.TYPE_UNTYPED_NAME + ")");
        names.put("test4", "test4(type1,type1)");
        names.put("test5", "test5(type1,type1,type2)");
        names.put("test6", "test6(): type3");
        names.put("test7", "test7(): type4");
        names.put("test8", "test8(type5): type6");
        names.put("test9", String.format("test9(%1$s,%1$s,%1$s): type7", PsiUtil.TYPE_UNTYPED_NAME));
        myFixture.configureByFiles("normalizeRoutineName.pas");
        Collection<PascalNamedElement> symbols = PascalParserUtil.findSymbols(myFixture.getProject(), "test");
        for (PascalNamedElement symbol : symbols) {
            if (symbol instanceof PascalRoutineImpl) {
                assertEquals(names.get(symbol.getName()), PsiUtil.normalizeRoutineName((PascalRoutineImpl) symbol));
            }
        }

    }
}
