package com.siberika.idea.pascal;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;

public class CalcTypeTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/annotator";
    }

    public void testFindSymbol() throws PasInvalidScopeException {
        myFixture.configureByFiles("structTypes.pas", "calcTypeTest.pas");
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(), myModule, "calcTypeTest");
        for (PasField field : mod.getAllFields()) {
            printIdent(field);
        }
    }

    private void printIdent(PasField field) throws PasInvalidScopeException {
        PasReferenceUtil.retrieveFieldTypeScope(field);
        System.out.println(String.format("%s: %s", field.name, field.getValueType()));
    }
}
