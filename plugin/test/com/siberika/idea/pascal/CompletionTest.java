package com.siberika.idea.pascal;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class CompletionTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/completion";
    }

    @Test
    public void testCompletion() {
        myFixture.configureByFiles("completionTest.pas");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertTrue(strings.containsAll(Arrays.asList("a", "b")));
    }

}
