package com.siberika.idea.pascal;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class AnnotatorTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/annotator";
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testAnnotator() {
        myFixture.configureByFiles("annotatorTest.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testExpression() {
        myFixture.configureByFiles("expression.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testTypes() {
        myFixture.configureByFiles("types.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testStructTypes() {
        myFixture.configureByFiles("structTypes.pas");
        myFixture.checkHighlighting(false, false, true);
    }

}
