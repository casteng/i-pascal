package com.siberika.idea.pascal;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
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

    public void testSimple() throws Exception {
        myFixture.configureByFiles("infereTypeSimple.pas");
        List<PasExpression> expressions = getStatementExpressions("infereTypeSimple");
        for (PasExpression expression : expressions) {
            System.out.println(String.format("%s: %s", expression.getText(), PascalExpression.infereType(expression.getExpr())));
        }
        int i = 0;
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TEnum", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("PEnum", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("array of TArr", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Single", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("String", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Boolean", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Pointer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TDatetime", PascalExpression.infereType(expressions.get(i++).getExpr()));
    }

    public void testPath() throws Exception {
        myFixture.configureByFiles("infereTypePath.pas");
        List<PasExpression> expressions = getStatementExpressions("infereTypePath");
        for (PasExpression expression : expressions) {
            String type = PascalExpression.infereType(expression.getExpr());
            System.out.println(String.format("%s: %s", expression.getText(), type));
        }
        int i = 0;
        assertEquals("TOuterRec", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TEnum", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TClass2", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("array of TInnerRec", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TOuterRec", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TInnerRec", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TEnum", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TInnerRec", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TInnerRec", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TClass1", PascalExpression.infereType(expressions.get(i++).getExpr()));
    }

    public void testComplex() throws Exception {
        myFixture.configureByFiles("infereTypeComplex.pas");
        List<PasExpression> expressions = getStatementExpressions("infereTypeComplex");
        for (PasExpression expression : expressions) {
            System.out.println(String.format("%s: %s", expression.getText(), PascalExpression.infereType(expression.getExpr())));
        }
        int i = 0;
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Single", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Pointer", PascalExpression.infereType(expressions.get(i++).getExpr()));

        assertEquals("Boolean", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("SomeType", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TRec", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("SomeType", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Word", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));

        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Int64", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Int64", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Int64", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("QWord", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Word", PascalExpression.infereType(expressions.get(i++).getExpr()));

        assertEquals("Single", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Single", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Double", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("Extended", PascalExpression.infereType(expressions.get(i++).getExpr()));
        assertEquals("TRec", PascalExpression.infereType(expressions.get(i++).getExpr()));
    }

    private List<PasExpression> getStatementExpressions(String unitName) {
        List<PasExpression> res = new ArrayList<PasExpression>();
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), unitName);
        Collection<PasStatement> stmts = PsiTreeUtil.findChildrenOfType(mod, PasStatement.class);
        for (PasStatement stmt : stmts) {
            if (stmt.getClass() == PasStatementImpl.class) {
                PasExpression expr = PsiTreeUtil.findChildOfType(stmt, PasExpression.class);
                if (expr != null) {
                    res.add(expr);
                }
            }
        }
        return res;
    }

}
