package com.siberika.idea.pascal;

import com.intellij.testFramework.ParsingTestCase;
import org.junit.Test;

/**
 * Author: George Bakhtadze
 * Date: 20/03/2013
 */
public class Test1 extends ParsingTestCase {
    public Test1() {
        super("", "pas", new PascalParserDefinition());
    }

    @Test
    public void testSimple() {
        doTest(true);
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }
}
