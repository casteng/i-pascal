package com.siberika.idea.pascal;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        checkCompletion(myFixture, "unit", "program", "library", "package", "begin ");
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
        myFixture.completeBasic();
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        List<String> exp = Arrays.asList(expected);
        ArrayList<String> lacking = new ArrayList<String>(exp);
        lacking.removeAll(strings);
        assertTrue(String.format("\nExpected to present: %s\nLack of: %s", exp, lacking), strings.containsAll(exp));
    }

    private void checkCompletionNotContains(CodeInsightTestFixture myFixture, String...unexpected) {
        completeBasicAllCarets(myFixture);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        List<String> unexp = Arrays.asList(unexpected);
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            if (unexp.contains(string)) {
                sb.append(String.format("\nUnexpected but present: %s", string));
            }
        }
        assertTrue(sb.toString(), sb.length() == 0);
    }

    public final List<LookupElement> completeBasicAllCarets(CodeInsightTestFixture myFixture) {
        final CaretModel caretModel = myFixture.getEditor().getCaretModel();
        final List<Caret> carets = caretModel.getAllCarets();

        final List<Integer> originalOffsets = new ArrayList<Integer>(carets.size());

        for (final Caret caret : carets) {
            originalOffsets.add(caret.getOffset());
        }
        caretModel.removeSecondaryCarets();

        // We do it in reverse order because completions would affect offsets
        // i.e.: when you complete "spa" to "spam", next caret offset increased by 1
        Collections.reverse(originalOffsets);
        final List<LookupElement> result = new ArrayList<LookupElement>();
        for (final int originalOffset : originalOffsets) {
            caretModel.moveToOffset(originalOffset);
            final LookupElement[] lookupElements = myFixture.completeBasic();
            if (lookupElements != null) {
                result.addAll(Arrays.asList(lookupElements));
            }
        }
        return result;
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

    public void testUnitDeclSectionImpl() {
        myFixture.configureByFiles("unitDeclSectionImpl.pas");
        checkCompletion(myFixture, "const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor",
                "uses", "begin", "initialization", "finalization");
        myFixture.type('v');
        checkCompletion(myFixture, "var", "threadvar");
    }

    public void testModuleSection() {
        myFixture.configureByFiles("moduleSection.pas");
        checkCompletion(myFixture, "const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor",
                "uses", "begin  ");
        myFixture.type('d');
        checkCompletion(myFixture, "destructor", "procedure", "threadvar");
    }

    public void testModuleSectionWithUses() {
        myFixture.configureByFiles("moduleSectionWithUses.pas");
        checkCompletion(myFixture, "const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor", "begin");
        myFixture.type('i');
        checkCompletion(myFixture, "function", "resourcestring");
    }

    public void testLocalDeclSection() {
        myFixture.configureByFiles("localDecl.pas");
        checkCompletion(myFixture, "const", "type", "var", "procedure", "function",
                "abstract", "assembler", "cdecl", "deprecated", "dispid", "dynamic", "experimental",
                "export", "final", "inline", "library", "message", "overload", "override", "pascal", "platform",
                "register", "reintroduce", "safecall", "static", "stdcall", "virtual", "begin");
        myFixture.type('c');
        checkCompletionContains(myFixture, "const", "procedure", "function");
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

    public void testStatement() {
        myFixture.configureByFiles("statement.pas");
        checkCompletionContains(myFixture, "a", "b", "s1",
                "for", "while", "repeat", "if", "case", "with",
                "goto", "exit", "try", "raise", "end");
        myFixture.type("i");
        checkCompletionContains(myFixture, "while", "if", "with", "exit", "raise");
    }

    public void testStatementInStmt() {
        myFixture.configureByFiles("statementInStmt.pas");
        /*checkCompletionNotContains(myFixture,
                "for", "while", "repeat", "if", "case", "with",
                "goto", "exit", "try", "raise", "begin", "end");*/
        checkCompletionContains(myFixture, "do", "then", "of");
    }

    public void testStatementInExpr() {
        myFixture.configureByFiles("statementInExpr.pas");
        checkCompletionNotContains(myFixture,
                "for", "while", "repeat", "if", "case", "with",
                "goto", "exit", "try", "raise", "end");
    }

    public void testRoutineHead() {
        myFixture.configureByFiles("routineHead.pas");
        checkCompletion(myFixture, "unit", "program", "library", "package", "begin");
    }

    public void testDcu() {
        myFixture.configureByFiles("dcu.pas");
        checkCompletionContains(myFixture, "spec", "v", "test", "proc");
    }

    public void testBeginend() {
        myFixture.configureByFiles("beginend.pas");
        checkCompletionContains(myFixture, "begin");
    }

    public void testElse() {
        myFixture.configureByFiles("else.pas");
        checkCompletionContains(myFixture, "else");
    }

}
