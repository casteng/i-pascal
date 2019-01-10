package com.siberika.idea.pascal;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.impl.PasStatementImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InfereTypeTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/typeInference";
    }

    public void testSimple() {
        myFixture.configureByFiles("infereTypeSimple.pas");
        List<PasExpr> expressions = getStatementExpressions("infereTypeSimple");
        for (PasExpr expression : expressions) {
            System.out.println(String.format("%s: %s", expression.getText(), PascalExpression.inferType(expression)));
        }
        int i = 0;
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TEnum", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("PEnum", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("array of TArr", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Single", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("String", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Boolean", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Pointer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TDatetime", PascalExpression.inferType(expressions.get(i++)));
    }

    public void testPath() {
        myFixture.configureByFiles("infereTypePath.pas");
        List<PasExpr> expressions = getStatementExpressions("infereTypePath");
        for (PasExpr expression : expressions) {
            String type = PascalExpression.inferType(expression);
            System.out.println(String.format("%s: %s", expression.getText(), type));
        }
        int i = 0;
        assertEquals("TOuterRec", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TEnum", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TClass2", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("array of TInnerRec", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TOuterRec", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TInnerRec", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TEnum", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TInnerRec", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TInnerRec", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TClass1", PascalExpression.inferType(expressions.get(i++)));
    }

    public void testComplex() {
        myFixture.configureByFiles("infereTypeComplex.pas");
        List<PasExpr> expressions = getStatementExpressions("infereTypeComplex");
        for (PasExpr expression : expressions) {
            System.out.println(String.format("%s: %s", expression.getText(), PascalExpression.inferType(expression)));
        }
        int i = 0;
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Single", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Pointer", PascalExpression.inferType(expressions.get(i++)));

        assertEquals("Boolean", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("SomeType", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TRec", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("SomeType", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Word", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));

        assertEquals("Integer", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Int64", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Int64", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Int64", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("QWord", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Word", PascalExpression.inferType(expressions.get(i++)));

        assertEquals("Single", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Single", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Double", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("Extended", PascalExpression.inferType(expressions.get(i++)));
        assertEquals("TRec", PascalExpression.inferType(expressions.get(i++)));
    }

    public void testCall() {
        myFixture.configureByFiles("infereTypeCall.pas");
        List<PasExpr> expressions = getCallExpressions("infereTypeCall");
        for (PasExpr expression : expressions) {
            String type = PascalExpression.calcFormalParameterType(expression);
            System.out.println(String.format("%s: %s", expression.getText(), type));
        }
        int i = 0;
        assertEquals("PEnum", PascalExpression.calcFormalParameterType(expressions.get(i++)));
        assertEquals("TEnum", PascalExpression.calcFormalParameterType(expressions.get(i++)));
        assertEquals("PEnum", PascalExpression.calcFormalParameterType(expressions.get(i++)));
    }

    private List<PasExpr> getStatementExpressions(String unitName) {
        List<PasExpr> res = new ArrayList<PasExpr>();
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), unitName);
        Collection<PasStatement> stmts = PsiTreeUtil.findChildrenOfType(mod, PasStatement.class);
        for (PasStatement stmt : stmts) {
            if (stmt.getClass() == PasStatementImpl.class) {
                PasExpression expr = PsiTreeUtil.findChildOfType(stmt, PasExpression.class);
                if (expr != null) {
                    res.add(expr.getExpr());
                }
            }
        }
        return res;
    }

    private List<PasExpr> getCallExpressions(String unitName) {
        List<PasExpr> res = new ArrayList<PasExpr>();
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), unitName);
        for (PasCallExpr callExpr : PsiTreeUtil.findChildrenOfType(mod, PasCallExpr.class)) {
            res.addAll(callExpr.getArgumentList().getExprList());
        }
        return res;
    }

}
