package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
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

    public void testFindSymbol() {
        myFixture.configureByFiles("gotoSymbolTest.pas");
        Collection<PascalNamedElement> symbols = TestUtil.findSymbols(myFixture.getProject(), "");
        Set<String> names = new HashSet<>(symbols.size());
        for (PascalNamedElement symbol : symbols) {
            names.add(symbol.getName());
        }
        assertEquals(new HashSet<>(Arrays.asList("a", "b", "c", "d")), names);
    }

    private static final String ACT_VAR = "Declare variable";
    private static final String ACT_TYPE = "Declare type";

    public void testAddActions() {
        myFixture.configureByFiles("addActions.pas");
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), "addActions");

        Map<String, PascalActionDeclare> fixesMap = getFixesMap();
        int i = 1;
        String text = myFixture.getDocument(mod.getContainingFile()).getText();
        boolean pass = true;
        for (String s : Arrays.asList("global." + ACT_VAR, "T." + ACT_TYPE, "b." + ACT_VAR, "t1." + ACT_TYPE)) {
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
        Map<String, PascalActionDeclare> map = new HashMap<>();
        for (IntentionAction fix : fixes) {
            if (fix instanceof PascalActionDeclare) {
                PascalActionDeclare ad = (PascalActionDeclare) fix;
                map.put(getData(ad).name + "." + ad.getText(), ad);
            }
        }
        System.out.println("Idents: " + map.keySet().toString());
        return map;
    }

    private Map<String, PascalNamedElement> getIdentsMap(PascalModuleImpl mod) {
        Collection<PasFullyQualifiedIdent> idents = PsiTreeUtil.findChildrenOfType(mod, PasFullyQualifiedIdent.class);
        Map<String, PascalNamedElement> identMap = new HashMap<>();
        for (PasFullyQualifiedIdent ident : idents) {
            identMap.put(ident.getName(), ident);
        }
        System.out.println("Idents: " + identMap.keySet().toString());
        return identMap;
    }
}
