package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasVarDeclaration;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;

import java.util.Collection;

public class AccessDetectorTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/misc";
    }

    public void testSectionToggle() {
        myFixture.configureByFiles("accessDetector.pas");
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), "accessDetector");
        doTestRefs(PsiTreeUtil.findChildrenOfAnyType(mod, PasNamedIdent.class, PasSubIdent.class, PasRefNamedIdent.class));
        doTestDecls(PsiTreeUtil.findChildrenOfAnyType(mod, PasVarDeclaration.class, PasConstDeclaration.class));
    }

    private void doTestDecls(Collection<PascalPsiElement> decls) {
        PascalReadWriteAccessDetector ad = new PascalReadWriteAccessDetector();
        for (PascalPsiElement decl : decls) {
            PascalNamedElement ident = PsiTreeUtil.findChildOfType(decl, PasNamedIdent.class);
            ident = ident != null ? ident : PsiTreeUtil.findChildOfType(decl, PasRefNamedIdent.class);
            assertNotNull("Ident is null: " + decl, ident);
            boolean writable = (decl instanceof PasConstDeclaration) || (ident.getName().endsWith("W"));
            assertTrue(!writable || ad.isDeclarationWriteAccess(ident));
        }
    }

    private void doTestRefs(Collection<PascalNamedElement> symbols) {
        for (PascalNamedElement symbol : symbols) {
            boolean res = PascalReadWriteAccessDetector.isWriteAccess(symbol);
            assertTrue(symbol.getName().endsWith("W") || !res);
        }
    }

}
