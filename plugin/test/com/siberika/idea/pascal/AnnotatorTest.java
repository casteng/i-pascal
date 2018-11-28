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

    public void testStatement() {
        myFixture.configureByFiles("statement.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testExpressionTypes() {
        myFixture.configureByFiles("calcTypesTest.pas", "types.pas", "structTypes.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testGenerics() {
        myFixture.configureByFiles("generics.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testScoped() {
        myFixture.configureByFiles("scoped.pas", "scoped.prog.pas", "scoped.types.pas", "scoped.util.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testInterfaces() {
        myFixture.configureByFiles("interfaces.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testObjects() {
        myFixture.configureByFiles("objects.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testClosure() {
        myFixture.configureByFiles("closure.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testOperators() {
        myFixture.configureByFiles("operators.pas");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testRecordConst() {
        myFixture.configureByFiles("recordConst.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testVariables() {
        myFixture.configureByFiles("variables.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testConstants() {
        myFixture.configureByFiles("constants.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testException() {
        myFixture.configureByFiles("exception.pas");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testClasses() {
        myFixture.configureByFiles("classesTest.pas");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testRoutines() {
        myFixture.configureByFiles("routines.pas");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testRoutinesForward() {
        myFixture.configureByFiles("routinesForward.pas");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testRoutinesNestedForward() {
        myFixture.configureByFiles("routinesNestedForward.pas");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testNestedMembers() {
        myFixture.configureByFiles("nestedMembers.pas");
        myFixture.checkHighlighting(false, false, true);
    }

    public void testAllowedKeywords() {
        myFixture.configureByFiles("allowedKeywords.pas");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testLibrary() {
        myFixture.configureByFiles("library1.pas");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testEnumTypes() {
        myFixture.configureByFiles("enumTypes.pas");
        myFixture.checkHighlighting(false, false, false);
    }

    public void testParamsInDeclaration() {
        myFixture.configureByFiles("paramsInDeclaration.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testClassParentWith() {
        myFixture.configureByFiles("classParentWith.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testNestedRoutines() {
        myFixture.configureByFiles("nestedRoutines.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testKeywordIdentifiers() {
        myFixture.configureByFiles("keywordIdentifiers.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testFPCGenerics() {
        myFixture.configureByFiles("fpcGenerics.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testVariant() {
        myFixture.configureByFiles("variant.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testHelpers() {
        myFixture.configureByFiles("helpers.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testUnitName() {
        myFixture.configureByFiles("unitName.pas");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testInlineDeclaration() {
        myFixture.configureByFiles("inlineDecl.pas");
        myFixture.checkHighlighting(false, false, true);
    }

}
