package com.siberika.idea.pascal;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CompletionTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/completion";
    }

    public void testCompletion() {
        myFixture.configureByFiles("completionTest.pas");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertEquals(Arrays.asList("r1"), strings);
    }

    public void testUnitCompletion() {
        myFixture.configureByFiles("usesCompletion.pas", "unit1.pas", "completionTest.pas");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertEquals(Arrays.asList("unit1"), strings);
    }

    public void testModuleHeadCompletion() {
        myFixture.configureByFiles("empty.pas");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertEquals(new HashSet<String>(Arrays.asList("unit", "program", "library", "package")), new HashSet<String>(strings));
    }

    public void testNoModuleHeadCompletion() {
        myFixture.configureByFiles("unit1.pas");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertEquals(Collections.emptySet(), new HashSet<String>(strings));
    }

    public void testUnitSection() {
        myFixture.configureByFiles("unitSections.pas");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertEquals(new HashSet<String>(Arrays.asList("interface", "implementation", "initialization", "finalization")), new HashSet<String>(strings));
    }

    public void testUnitDeclSection() {
        myFixture.configureByFiles("unitDeclSection.pas");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertEquals(new HashSet<String>(Arrays.asList("const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor",
                "uses"
        )), new HashSet<String>(strings));
    }

    public void testModuleSection() {
        myFixture.configureByFiles("moduleSection.pas");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertEquals(new HashSet<String>(Arrays.asList("const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor",
                "uses"
        )), new HashSet<String>(strings));
    }

    public void testModuleSectionWithUses() {
        myFixture.configureByFiles("moduleSectionWithUses.pas");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertEquals(new HashSet<String>(Arrays.asList("const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor"
        )), new HashSet<String>(strings));
    }
}
