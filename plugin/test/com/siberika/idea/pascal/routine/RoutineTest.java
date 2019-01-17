package com.siberika.idea.pascal.routine;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.ide.actions.PascalDefinitionsSearch;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.TestUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class RoutineTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "testData/routine";
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testFindSubMethods() {
        myFixture.configureByFiles("findSubMethods.pas");
        PasEntityScope parent = TestUtil.findClass(Objects.requireNonNull(PsiUtil.getElementPasModule(myFixture.getFile())), "TParent");
        PasField r = parent.getField("test");
        PascalDefinitionsSearch.findImplementations((r.getElement()).getNameIdentifier(), new Processor<PasEntityScope>() {
            @Override
            public boolean process(PasEntityScope element) {
                assertEquals(element.getContainingScope(), TestUtil.findClass(Objects.requireNonNull(PsiUtil.getElementPasModule(myFixture.getFile())), "TChild"));
                return false;
            }
        });
    }

    public void testNormalizeRoutineName() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("test1", "test1()");
        names.put("test2", "test2()");
        names.put("test3", "test3(" + PsiUtil.TYPE_UNTYPED_NAME + ")");
        names.put("test4", "test4(type1,type1)");
        names.put("test5", "test5(type1,type1,type2)");
        names.put("test6", "test6():type3");
        names.put("test7", "test7():string");
        names.put("test8", "test8(type5):type6");
        names.put("test9", String.format("test9(%1$s,%1$s,%1$s):type7", PsiUtil.TYPE_UNTYPED_NAME));
        names.put("testA", "testA(typeA1<T>,typeA2):typeA3");
        names.put("testB", "testB(type1)");
        names.put("testC", "testC()");
        myFixture.configureByFiles("normalizeRoutineName.pas");
        Collection<PascalRoutine> symbols = TestUtil.findSymbols(myFixture.getProject(), "test", PascalRoutine.class);
        assertEquals("Wrong number of routines", names.size(), symbols.size());
        for (PascalNamedElement symbol : symbols) {
            if (symbol instanceof PascalRoutine) {
                System.out.println(String.format("%s = %s", symbol.getName(), PsiUtil.normalizeRoutineName((PascalRoutine) symbol)));
                assertEquals(names.get(symbol.getName()), PsiUtil.normalizeRoutineName((PascalRoutine) symbol));
            }
        }

    }

    public void testReduceRoutineName() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("test1", "test1(Type1Type)");
        names.put("test2", "test2(Type1Type)");
        names.put("test3", "test3(" + PsiUtil.TYPE_UNTYPED_NAME + ")");
        names.put("test4", "test4(Type1,Type1)");
        names.put("test5", "test5(Type1,Type1,Type2)");
        names.put("test6", "test6()");
        names.put("test7", "test7()");
        names.put("test8", "test8(Type1)");
        names.put("test9", "test9(Type1)");
        names.put("testA", "testA(Type1,Type2)");
        names.put("testB", "testB(Type2)");
        myFixture.configureByFiles("reduceRoutineName.pas");
        Collection<PascalRoutine> symbols = TestUtil.findSymbols(myFixture.getProject(), "test", PascalRoutine.class);
        assertEquals("Wrong number of routines", names.size(), symbols.size());
        for (PascalNamedElement symbol : symbols) {
            if (symbol instanceof PascalRoutine) {
                System.out.println(String.format("%s = %s", PsiUtil.normalizeRoutineName((PascalRoutine) symbol), ((PascalRoutine) symbol).getReducedName()));
                assertEquals(names.get(symbol.getName()), ((PascalRoutine) symbol).getReducedName());
            }
        }

    }

}
