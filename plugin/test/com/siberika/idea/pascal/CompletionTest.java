package com.siberika.idea.pascal;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class CompletionTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/completion";
    }

    public void testCompletion() {
        myFixture.configureByFiles("completionTest.pas");
        checkCompletion(myFixture, "r1");
    }

    public void testUnitCompletion() {
        myFixture.configureByFiles("usesCompletion.pas", "unit1.pas", "completionTest.pas");
        checkCompletion(myFixture, "unit1");
    }

    public void testModuleHeadCompletion() {
        myFixture.configureByFiles("empty.pas");
        checkCompletion(myFixture, "unit", "program", "library", "package");
        myFixture.type('p');
        checkCompletion(myFixture, "program", "package");
    }

    private void checkCompletion(CodeInsightTestFixture myFixture, String...expected) {
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        assertEquals(new TreeSet<String>(Arrays.asList(expected)), new TreeSet<String>(strings));
    }

    private void checkCompletionContains(CodeInsightTestFixture myFixture, String...expected) {
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        List<String> exp = Arrays.asList(expected);
        assertTrue(String.format("\nExpected to present: %s\nActual: %s", exp, strings), strings.containsAll(exp));
    }

    public void testNoModuleHeadCompletion() {
        myFixture.configureByFiles("unit1.pas");
        checkCompletion(myFixture);
    }

    public void testUnitSection() {
        myFixture.configureByFiles("unitSections.pas");
        checkCompletion(myFixture, "interface", "implementation", "initialization", "finalization");
        myFixture.type('f');
        checkCompletion(myFixture, "interface", "finalization");
    }

    public void testUnitDeclSection() {
        myFixture.configureByFiles("unitDeclSection.pas");
        checkCompletion(myFixture, "const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor",
                "uses", "begin");
        myFixture.type('v');
        checkCompletion(myFixture, "var", "threadvar");
    }

    public void testModuleSection() {
        myFixture.configureByFiles("moduleSection.pas");
        checkCompletion(myFixture, "const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor",
                "uses");
        myFixture.type('d');
        checkCompletion(myFixture, "destructor", "procedure", "threadvar");
    }

    public void testModuleSectionWithUses() {
        myFixture.configureByFiles("moduleSectionWithUses.pas");
        checkCompletion(myFixture, "const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor");
        myFixture.type('i');
        checkCompletion(myFixture, "function", "resourcestring");
    }

    public void testLocalDeclSection() {
        myFixture.configureByFiles("localDecl.pas");
        checkCompletion(myFixture, "const", "type", "var", "procedure", "function");
        myFixture.type('c');
        checkCompletion(myFixture, "const", "procedure", "function");
    }

    public void testStructured() {
        myFixture.configureByFiles("structured.pas");
        checkCompletion(myFixture, "strict", "private", "protected", "public", "published", "automated",
                "procedure", "function", "constructor", "destructor",
                "class", "operator", "property", "end");
        myFixture.type('a');
        checkCompletion(myFixture, "automated", "private", "class", "operator");
    }

    public void testTypeId() {
        myFixture.configureByFiles("typeId.pas");
        checkCompletionContains(myFixture, "TTest", "TRec2", "typeId",
                "type", "class", "dispinterface", "interface ", "record", "object", "packed", "set", "file", "helper", "array");
        myFixture.type("te");
        checkCompletionContains(myFixture, "TTest", "dispinterface", "interface ");
    }

    public void testParent() {
        myFixture.configureByFiles("parent.pas");
        checkCompletion(myFixture, "ParentConstructor", "parentMethod", "ChildConstructor", "ChildMethod");
    }

}
