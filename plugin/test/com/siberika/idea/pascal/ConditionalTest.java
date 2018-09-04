package com.siberika.idea.pascal;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class ConditionalTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/conditional";
    }

    public void testIfdef() {
        myFixture.configureByFiles("ifdef.pas");
        CompletionTest.checkCompletionContains(myFixture, "active1", "active2", "active3");
        CompletionTest.checkCompletionNotContains(myFixture, "inactive1", "inactive2");
    }

    public void testIf() {
        myFixture.configureByFiles("if.pas");
        CompletionTest.checkCompletionContains(myFixture, "active1", "active2", "active3", "active4");
        CompletionTest.checkCompletionNotContains(myFixture, "inactive1", "inactive2", "inactive3", "inactive3a", "inactive4", "inactive4a");
    }

    public void testIfdefElseif() {
        myFixture.configureByFiles("ifdefElseif.pas");
        CompletionTest.checkCompletionContains(myFixture, "active1", "active2");
        CompletionTest.checkCompletionNotContains(myFixture, "inactive1", "inactive1a", "inactive2", "inactive2a");
    }

    public void testIfNested() {
        myFixture.configureByFiles("ifnested.pas");
        CompletionTest.checkCompletionContains(myFixture, "active1");
        CompletionTest.checkCompletionNotContains(myFixture, "inactive1", "inactive2", "inactive3");
    }

}
