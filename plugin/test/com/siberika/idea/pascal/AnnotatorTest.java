package com.siberika.idea.pascal;

import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.testng.annotations.Test;

public class AnnotatorTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/annotator";
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    @Test
    public void testAnnotator() {
        myFixture.configureByFiles("annotatorTest.pas");
        ApplicationEx app = ApplicationManagerEx.getApplicationEx();
        app.runWriteAction(new Runnable() {
            @Override
            public void run() {
                System.out.println("Hello");
            }
        });
//        myFixture.checkHighlighting(true, true, true, false);
        myFixture.checkHighlighting(false, false, true);
    }

}
