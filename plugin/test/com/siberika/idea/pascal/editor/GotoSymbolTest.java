package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.ide.actions.PascalDefinitionsSearch;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.TestUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GotoSymbolTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/gotoSymbol";
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testFindSubMethods() throws Exception {
        myFixture.configureByFiles("findSubMethods.pas");
        PasEntityScope parent = TestUtil.findClass(PsiUtil.getElementPasModule(myFixture.getFile()), "TParent");
        PasField r = parent.getField("test");
        Collection<PasEntityScope> impls = PascalDefinitionsSearch.findImplementations((r.getElement()).getNameIdentifier(), 100, 0);
        assertEquals(impls.iterator().next().getContainingScope(), TestUtil.findClass(PsiUtil.getElementPasModule(myFixture.getFile()), "TChild"));
    }

    public void testFindSymbol() {
        myFixture.configureByFiles("gotoSymbolTest.pas");
        Collection<PascalNamedElement> symbols = PascalParserUtil.findSymbols(myFixture.getProject(), "");
        Set<String> names = new HashSet<String>(symbols.size());
        for (PascalNamedElement symbol : symbols) {
            names.add(symbol.getName());
        }
        assertEquals(new HashSet<String>(Arrays.asList("a", "b", "c", "d")), names);
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
        names.put("testA", "testA(typeA1<T>,typeA2): typeA3");
        myFixture.configureByFiles("normalizeRoutineName.pas");
        Collection<PascalRoutineImpl> symbols = PascalParserUtil.findSymbols(myFixture.getProject(), "test", PascalRoutineImpl.class);
        assertEquals("Wrong number of routines", names.size(), symbols.size());
        for (PascalNamedElement symbol : symbols) {
            if (symbol instanceof PascalRoutineImpl) {
                System.out.println(String.format("%s = %s", symbol.getName(), PsiUtil.normalizeRoutineName((PascalRoutineImpl) symbol)));
                assertEquals(names.get(symbol.getName()), PsiUtil.normalizeRoutineName((PascalRoutineImpl) symbol));
            }
        }

    }

    public void testAddActions() throws Exception {
        myFixture.configureByFiles("addActions.pas");
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), "addActions");

        Map<String, PascalActionDeclare> fixesMap = getFixesMap();
        int i = 1;
        String text = myFixture.getDocument(mod.getContainingFile()).getText();
        boolean pass = true;
        for (String s : Arrays.asList("global", "T", "b", "t1")) {
            int offs = text.indexOf(String.format("{%d<}", i));
            if (offs < 0) {
                offs = text.indexOf(String.format("{%d>}", i)) + 4;
            }
            PascalActionDeclare action = fixesMap.get(s);
            action.calcData(mod.getContainingFile(), getData(action));
            if (offs != getData(action).offset) {
                System.out.println(String.format("%s(%d): expected offset %d, actual %d", s, i, offs, getData(action).offset));
                pass = false;
            }
            i++;
        }
        assertEquals(true, pass);
    }

    private PascalActionDeclare.FixActionData getData(PascalActionDeclare ad) {
        return ad.fixActionDataArray.iterator().next();
    }

    private Map<String, PascalActionDeclare> getFixesMap() {
        List<IntentionAction> fixes = myFixture.getAllQuickFixes("addActions.pas");
        Map<String, PascalActionDeclare> map = new HashMap<String, PascalActionDeclare>();
        for (IntentionAction fix : fixes) {
            if (fix instanceof PascalActionDeclare) {
                PascalActionDeclare ad = (PascalActionDeclare) fix;
                map.put(getData(ad).element.getName(), ad);
            }
        }
        System.out.println("Idents: " + map.keySet().toString());
        return map;
    }

    private Map<String, PascalNamedElement> getIdentsMap(PascalModuleImpl mod) {
        Collection<PasFullyQualifiedIdent> idents = PsiTreeUtil.findChildrenOfType(mod, PasFullyQualifiedIdent.class);
        Map<String, PascalNamedElement> identMap = new HashMap<String, PascalNamedElement>();
        for (PasFullyQualifiedIdent ident : idents) {
            identMap.put(ident.getName(), ident);
        }
        System.out.println("Idents: " + identMap.keySet().toString());
        return identMap;
    }
}
