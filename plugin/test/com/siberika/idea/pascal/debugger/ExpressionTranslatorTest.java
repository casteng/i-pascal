package com.siberika.idea.pascal.debugger;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

public class ExpressionTranslatorTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "testData/reference";
    }

    public void testParse() {
        Assert.assertEquals("(*PTR)", checkExpr("ptr^"));
        Assert.assertEquals("&(VALUE)", checkExpr("@value"));
        Assert.assertEquals("VALUE % 0x5", checkExpr("value mod $5"));
        Assert.assertEquals("this.FIELD == 8", checkExpr("self.field = 8"));
        Assert.assertEquals("VALUE + 5", checkExpr("value + 5"));
        Assert.assertEquals("VALUE / 15", checkExpr("value div 15"));
        Assert.assertEquals("VALUE & 5", checkExpr("value and 5"));
        Assert.assertEquals("(VALUE << 2) | true", checkExpr("(value shl 2) or true"));
        Assert.assertEquals("((TOBJECT)VALUE)", checkExpr("value as TObject"));
        Assert.assertEquals("(*TEST.DATA)[0 - $$TEST.$$PARENT.SIZE]", checkExpr("test.data^[0-$$test.$$parent.size]"));
        Assert.assertEquals("(*TEST.DATA)", checkExpr("test.data^[0+1*2..$$test.$$parent.size-1]"));
        Assert.assertEquals("\"string's\"", checkExpr("'string''s'"));
    }

    private String checkExpr(String str) {
        PascalCExpressionTranslator translator = new PascalCExpressionTranslator();
        TranslatedExpression res = translator.translate(str, myFixture.getProject());
        return res.getExpression();
    }

}
