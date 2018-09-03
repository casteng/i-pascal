package com.siberika.idea.pascal;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.xdebugger.XDebuggerUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
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

    public static void checkCompletionContains(CodeInsightTestFixture myFixture, String...expected) {
        myFixture.completeBasic();
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings != null);
        checkContains(strings, expected, "\nMust present: ");
    }

    private void checkCompletionContainsAllCarets(CodeInsightTestFixture myFixture, String...expected) {
        List<List<String>> strings = completeBasicAllCarets(myFixture);
        for (int i = 0; i < strings.size(); i++) {
            checkContains(strings.get(i), expected, String.format("\nCaret #%d, must present: ", strings.size()-i));
        }
    }

    private void checkCompletionMatchCarets(CodeInsightTestFixture myFixture, String...expected) {
        List<List<String>> strings = completeBasicAllCarets(myFixture);
        assertEquals(expected.length, strings.size());
        for (int i = 0; i < strings.size(); i++) {
            String exp = expected[strings.size() - i - 1];
            List<String> actual = strings.get(i);
            assertTrue(String.format("\nCaret #%d expected match %s, actual: %s",
                    strings.size() - i, exp, actual.isEmpty() ? "<empty>" : actual.get(0)),
                    (actual.size() == 1) && exp.equalsIgnoreCase(actual.get(0)));
        }
    }

    private static void checkContains(List<String> strings, String[] expected, String prefix) {
        List<String> exp = Arrays.asList(expected);
        ArrayList<String> lacking = new ArrayList<String>(exp);
        lacking.removeAll(strings);
        assertTrue(String.format(prefix + "%s\nLack of: %s", exp, lacking), strings.containsAll(exp));
    }

    public static void checkCompletionNotContains(CodeInsightTestFixture myFixture, String...unexpected) {
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

    public static final List<List<String>> completeBasicAllCarets(CodeInsightTestFixture myFixture) {
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
        final List<List<String>> result = new ArrayList<List<String>>(originalOffsets.size());
        for (final int originalOffset : originalOffsets) {
            caretModel.moveToOffset(originalOffset);
            final LookupElement[] lookupElements = myFixture.completeBasic();
            if (lookupElements != null) {
                List<String> res = new ArrayList<String>(lookupElements.length);
                result.add(res);
                for (LookupElement lookupElement : lookupElements) {
                    res.add(lookupElement.getLookupString());
                }
            } else {
                result.add(Collections.<String>emptyList());
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
                "procedure", "function", "uses");
        myFixture.type('v');
        checkCompletion(myFixture, "var", "threadvar");
    }

    public void testUnitDeclSectionImpl() {
        myFixture.configureByFiles("unitDeclSectionImpl.pas");
        checkCompletion(myFixture, "const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor",
                "uses", "begin  ", "initialization", "finalization");
        myFixture.type('v');
        checkCompletion(myFixture, "var", "threadvar");
    }

    public void testModuleSection() {
        myFixture.configureByFiles("moduleSection.pas");
        checkCompletion(myFixture, "const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor", "uses", "begin  ");
        myFixture.type('d');
        checkCompletion(myFixture, "destructor", "procedure", "threadvar");
    }

    public void testModuleSectionWithUses() {
        myFixture.configureByFiles("moduleSectionWithUses.pas");
        checkCompletion(myFixture, "const", "type", "var", "threadvar", "resourcestring",
                "procedure", "function", "constructor", "destructor", "begin  ");
        myFixture.type('i');
        checkCompletion(myFixture, "function", "resourcestring", "begin  ");
    }

    public void testRoutineHead() {
        myFixture.configureByFiles("routineHead.pas");
        checkCompletion(myFixture, "assembler", "cdecl", "deprecated", "experimental",
                "export", "inline", "library", "overload", "pascal", "platform",
                "register", "safecall", "stdcall", "begin");
    }

    public void testRoutineBlock() {
        myFixture.configureByFiles("routineBlock.pas");
        checkCompletion(myFixture, "const", "type", "var", "procedure", "function", "begin");
    }

    public void testRoutineParams1() {
        myFixture.configureByFiles("routineParams1.pas");
        checkCompletionContainsAllCarets(myFixture, "const ", "var ", "out ");
    }

    public void testRoutineParams2() {
        myFixture.configureByFiles("routineParams2.pas");
        checkCompletion(myFixture, "const ", "var ", "out ");
        myFixture.type('o');
        checkCompletion(myFixture, "const ", "out ");
    }

    public void testRoutineParams3() {
        myFixture.configureByFiles("routineParams3.pas");
        checkCompletionNotContains(myFixture, "const ", "var ", "out ");
    }

    public void testMethodDirectivesIntf() {
        myFixture.configureByFiles("methodDirectivesIntf.pas");
        checkCompletion(myFixture, "assembler", "cdecl", "deprecated", "experimental",
                "export", "final", "inline", "library", "message", "overload", "pascal", "platform",
                "register", "safecall", "static", "stdcall",
                "abstract", "dynamic", "override", "reintroduce", "virtual");
        myFixture.type('v');
        checkCompletionContains(myFixture, "virtual", "overload", "override");
    }

    public void testStructIntf() {
        myFixture.configureByFiles("structIntf.pas");
        checkCompletion(myFixture, "const", "type", "var", "procedure", "function", "constructor", "destructor",
                "strict private", "strict protected", "private", "protected", "public", "published", "automated",
                "class ", "operator", "property", "end");
        myFixture.type('v');
        checkCompletion(myFixture, "var", "private", "strict private");
    }

    public void testMethodDeclImplHead() {
        myFixture.configureByFiles("methodImplHead.pas");
        checkCompletion(myFixture, "begin");
    }

    public void testMethodDeclImplHeadNewLine() {
        myFixture.configureByFiles("methodImplHeadNewLine.pas");
        checkCompletion(myFixture, "begin", "const", "function", "procedure", "type", "var");
    }

    public void testMethodDeclImpl() {
        myFixture.configureByFiles("methodImpl.pas");
        checkCompletion(myFixture, "const", "type", "var", "procedure", "function", "begin");
        myFixture.type('t');
        checkCompletion(myFixture, "type", "const", "function");
    }

    public void testStructured() {
        myFixture.configureByFiles("structured.pas");
        checkCompletion(myFixture, "strict private", "strict protected", "private", "protected", "public", "published", "automated",
                "const", "type", "var", "procedure", "function", "constructor", "destructor",
                "class ", "operator", "property", "end");
        myFixture.type('a');
        checkCompletion(myFixture, "automated", "private", "strict private", "class ", "operator", "var");
    }

    public void testTypeIdInVarDecl() {
        myFixture.configureByFiles("typeId.pas");
        checkCompletionContains(myFixture, "TTest", "TRec2", "typeId",
                "record", "packed", "set", "file", "array");
        myFixture.type("re");
        checkCompletionContains(myFixture, "TRec2", "record");
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

    public void testConsts() {
        myFixture.configureByFiles("consts.pas");
        checkCompletionNotContains(myFixture, "a", "b");
        checkCompletionContains(myFixture, "CONST_1", "CONST_2");
    }

    public void testStatementInStmt() {
        myFixture.configureByFiles("statementInStmt.pas");
        checkCompletionMatchCarets(myFixture, "do", "then", "do", "of", "do");
    }

    public void testStatementInExpr() {
        myFixture.configureByFiles("statementInExpr.pas");
        checkCompletionNotContains(myFixture,
                "for", "while", "repeat", "if", "case", "with",
                "goto", "exit", "try", "raise", "end");
    }

    public void testDcu() {
        myFixture.configureByFiles("dcu.pas");
        checkCompletionContains(myFixture, "spec", "v", "test", "proc");
    }

    public void testBeginend() {
        myFixture.configureByFiles("beginend.pas");
        checkCompletionContainsAllCarets(myFixture, "begin");
    }

    public void testElse() {
        myFixture.configureByFiles("else.pas");
        checkCompletionContainsAllCarets(myFixture, "else");
    }

    public void testProp() {
        myFixture.configureByFiles("prop.pas");
        checkCompletionContains(myFixture, "X", "Y");
    }

    public void testContext() {
        myFixture.configureByFiles("empty.pas", "contextTest.pas");
        PsiElement el = XDebuggerUtil.getInstance().findContextElement(myFixture.findFileInTempDir("contextTest.pas"), 42, myFixture.getProject(), false);
        PasEntityScope scope = PsiUtil.getNearestAffectingScope(el);
        NamespaceRec fqn = NamespaceRec.fromFQN(myFixture.getFile(), "");
        fqn.setIgnoreVisibility(true);
        ResolveContext context = new ResolveContext(scope, EnumSet.of(PasField.FieldType.VARIABLE), false, null, null);
        Collection<PasField> fields = PasReferenceUtil.resolve(fqn, context, 0);
        assertTrue(fields.iterator().hasNext());
        assertEquals("local", fields.iterator().next().name);
    }

    public void testForwardStructure() {
        myFixture.configureByFiles("forwardStructure.pas");
        checkCompletionContains(myFixture, "Bar");
    }

    public void testInherited() {
        myFixture.configureByFiles("inherited1.pas", "inherited2.pas");
        checkCompletion(myFixture, "ParentConstructor", "parentMethod", "Parent2Constructor", "parent2Method");
    }

}
