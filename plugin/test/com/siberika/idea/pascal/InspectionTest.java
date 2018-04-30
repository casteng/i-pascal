package com.siberika.idea.pascal;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.inspection.UnusedIdentsInspection;
import com.siberika.idea.pascal.lang.inspection.UnusedUnitsInspection;

@SuppressWarnings("unchecked")
public class InspectionTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/annotator";
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testUnusedUnits() {
        myFixture.enableInspections(UnusedUnitsInspection.class);
        myFixture.configureByFiles("unusedUnits.pas", "types.pas", "objects.pas", "scoped.types.pas", "exception.pas", "interfaces.pas", "routines.pas", "enumTypes.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testUsesInImplementation() {
        myFixture.enableInspections(UnusedUnitsInspection.class);
        myFixture.configureByFiles("usesInImplementation.pas", "types.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testUsesUnusedIdents() {
        myFixture.enableInspections(UnusedIdentsInspection.class);
        myFixture.configureByFiles("unusedIdents.pas");
        myFixture.checkHighlighting(true, false, false);
    }

}
