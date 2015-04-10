package com.siberika.idea.pascal;

import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
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
        ApplicationEx app = ApplicationManagerEx.getApplicationEx();
        app.runWriteAction(new Runnable() {
            @Override
            public void run() {
                System.out.println("Hello");
            }
        });
        myFixture.checkHighlighting(false, false, true);
    }

    public void testExpression() {
        myFixture.configureByFiles("expression.pas");
        ApplicationEx app = ApplicationManagerEx.getApplicationEx();
        myFixture.checkHighlighting(false, false, true);
    }

}
