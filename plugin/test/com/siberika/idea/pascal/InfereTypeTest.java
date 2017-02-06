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
            System.out.println(String.format("%s: %s", expression.getText(), PascalExpression.infereType(expression)));
        }
        int i = 0;
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("TEnum", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("PEnum", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("array of TArr", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("Single", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("String", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("Boolean", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("Pointer", PascalExpression.infereType(expressions.get(i++)));
    }

    public void testPath() throws Exception {
        myFixture.configureByFiles("infereTypePath.pas");
        List<PasExpression> expressions = getStatementExpressions("infereTypePath");
        for (PasExpression expression : expressions) {
            System.out.println(String.format("%s: %s", expression.getText(), PascalExpression.infereType(expression)));
        }
        int i = 0;
        assertEquals("array of TInnerRec", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("TOuterRec", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("TInnerRec", PascalExpression.infereType(expressions.get(i++)));
        assertEquals("TEnum", PascalExpression.infereType(expressions.get(i++)));
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
