package com.siberika.idea.pascal;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
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
        List<PascalExpression> expressions = getStatementExpressions("infereTypeSimple");
        for (PascalExpression expression : expressions) {
            System.out.println(String.format("%s: %s", expression.getText(), PascalExpression.infereType(expression)));
        }
        assertEquals("Integer", PascalExpression.infereType(expressions.get(0)));
        assertEquals("TEnum", PascalExpression.infereType(expressions.get(1)));
        assertEquals("PEnum", PascalExpression.infereType(expressions.get(2)));
        assertEquals("array of TArr", PascalExpression.infereType(expressions.get(3)));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(4)));
        assertEquals("Integer", PascalExpression.infereType(expressions.get(5)));
        assertEquals("Single", PascalExpression.infereType(expressions.get(6)));
        assertEquals("String", PascalExpression.infereType(expressions.get(7)));
        assertEquals("Boolean", PascalExpression.infereType(expressions.get(8)));
        assertEquals("Pointer", PascalExpression.infereType(expressions.get(9)));
    }

    private List<PascalExpression> getStatementExpressions(String unitName) {
        List<PascalExpression> res = new ArrayList<PascalExpression>();
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), unitName);
        Collection<PasStatement> stmts = PsiTreeUtil.findChildrenOfType(mod, PasStatement.class);
        for (PasStatement stmt : stmts) {
            if (stmt.getClass() == PasStatementImpl.class) {
                PascalExpression expr = PsiTreeUtil.findChildOfType(stmt, PascalExpression.class);
                if (expr != null) {
                    res.add(expr);
                }
            }
        }
        return res;
    }

}
