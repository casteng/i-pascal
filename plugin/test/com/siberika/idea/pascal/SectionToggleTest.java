package com.siberika.idea.pascal;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SectionToggleTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/misc";
    }

    public void test() {
        myFixture.configureByFiles("sectionToggle.pas");
        List<PascalNamedElement> symbols = new ArrayList<PascalNamedElement>(PascalParserUtil.findSymbols(myFixture.getProject(), ""));
        Collections.sort(symbols, new Comparator<PascalNamedElement>() {
            @Override
            public int compare(PascalNamedElement o1, PascalNamedElement o2) {
                return o1.getTextOffset() - o2.getTextOffset();
            }
        });

        Collection<PasExportedRoutineImpl> decls = getDecls(symbols);
        List<PascalRoutineImpl> impls = new ArrayList<PascalRoutineImpl>();
        for (PasExportedRoutineImpl decl : decls) {
            PsiElement impl = SectionToggle.retrieveImplementation(decl);
            printElement("Impl: " + decl.getName(), impl);
            impls.add((PascalRoutineImpl) impl);
        }

        for (PascalRoutineImpl impl : impls) {
            PsiElement decl = SectionToggle.retrieveDeclaration(impl);
            printElement("Decl: " + impl.getName(), decl);
        }
    }

    private void printElement(String name, PsiElement impl) {
        System.out.println(String.format("%s: %s", name, impl));
    }

    private Collection<PasExportedRoutineImpl> getDecls(Collection<PascalNamedElement> symbols) {
        Collection<PasExportedRoutineImpl> res = new ArrayList<PasExportedRoutineImpl>();
        for (PascalNamedElement symbol : symbols) {
            if (symbol instanceof PasExportedRoutineImpl) {
                res.add((PasExportedRoutineImpl) symbol);
            }
        }
        return res;
    }

}
