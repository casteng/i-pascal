package com.siberika.idea.pascal;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDeclNested1;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.util.TestUtil;

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

    public void testSectionToggle() {
        List<PascalNamedElement> symbols = retrieveSymbols("sectionToggle.pas");
        doTestSectionToggle(symbols, false);
    }

    public void testSectionToggleStrict() {
        List<PascalNamedElement> symbols = retrieveSymbols("sectionToggleStrict.pas");
        doTestSectionToggle(symbols, true);
    }

    public void testSectionToggleGeneric() {
        List<PascalNamedElement> symbols = retrieveSymbols("sectionToggleGeneric.pas");
        doTestSectionToggle(symbols, true);
    }

    public void testSectionToggleGenericConstrained() {
        List<PascalNamedElement> symbols = retrieveSymbols("sectionToggleGenericConstrained.pas");
        doTestSectionToggle(symbols, true);
    }

    public void testRoutinesFwd() {
        PsiElement ref = myFixture.configureByFile("routinesFwd.pas").findElementAt(95);
        PsiElement decl = SectionToggle.getRoutineForwardDeclaration((PasRoutineImplDeclImpl) ref.getParent().getParent());
        assertEquals(46, decl.getTextRange().getStartOffset());
    }

    private List<PascalNamedElement> retrieveSymbols(String filename) {
        myFixture.configureByFiles(filename);
        List<PascalNamedElement> symbols = new ArrayList<PascalNamedElement>(TestUtil.findSymbols(myFixture.getProject(), ""));
        Collections.sort(symbols, new Comparator<PascalNamedElement>() {
            @Override
            public int compare(PascalNamedElement o1, PascalNamedElement o2) {
                return o1.getTextOffset() - o2.getTextOffset();
            }
        });
        return symbols;
    }

    private void doTestSectionToggle(List<PascalNamedElement> symbols, boolean strict) {
        Collection<PasExportedRoutineImpl> decls = getDecls(symbols);
        List<PascalRoutine> impls = new ArrayList<PascalRoutine>();
        for (PascalNamedElement symbol : symbols) {
            if (symbol instanceof PasRoutineImplDeclNested1) {
                impls.add((PascalRoutine) symbol);
            }
        }
        for (PasExportedRoutineImpl decl : decls) {
            boolean invalid = isInvalid(decl);
            PascalRoutine impl = (PascalRoutine) SectionToggle.retrieveImplementation(decl, strict);
            assertTrue(String.format("Implementation of %s not found", decl.getName()), invalid || impl != null);
            assertTrue(String.format("Implementation of %s found but should not", decl.getName()), !invalid || (impl == null));
            printElement("Impl: " + decl.getName(), impl);
            assertTrue(String.format("Wrong implementation of %s found", decl.getName()), invalid || impl.getName().endsWith(decl.getName()));
            impls.add((PascalRoutine) SectionToggle.retrieveImplementation(decl, false));
        }

        for (PascalRoutine impl : impls) {
            boolean invalid = isInvalid(impl);
            PascalRoutine decl = (PascalRoutine) SectionToggle.retrieveDeclaration(impl, strict);
            assertTrue(String.format("Declaration of %s not found", impl.getName()), invalid || (decl != null));
            assertTrue(String.format("Declaration of %s found but should not", impl.getName()), !invalid || (decl == null));
            printElement("Decl: " + impl.getName(), decl);
            assertTrue(String.format("Wrong declaration of %s found", impl.getName()), invalid || impl.getName().endsWith(decl.getName()));
        }
    }

    private boolean isInvalid(PascalRoutine impl) {
        return impl.getName().endsWith("invalid");
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
