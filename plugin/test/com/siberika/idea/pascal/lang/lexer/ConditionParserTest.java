package com.siberika.idea.pascal.lang.lexer;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 25/08/2018
 */
public class ConditionParserTest {

    @Test
    public void testParseSimple() {
        Set<String> def = new HashSet<>(Arrays.asList("DEF1", "DEF2"));
        Assert.assertTrue(ConditionParser.checkCondition("defined(def1)", def));
        Assert.assertTrue(ConditionParser.checkCondition("defined(def1) or Defined(undef)", def));
        Assert.assertTrue(ConditionParser.checkCondition("defined(def1) and defined(def2)", def));
        Assert.assertTrue(ConditionParser.checkCondition("defined(def1) OR defined(undef) and defined(def2)", def));

        Assert.assertTrue(ConditionParser.checkCondition("not defined(def1) OR not defined(undef) and not defined(undef)", def));

        Assert.assertFalse(ConditionParser.checkCondition("DEFINED(undef)", def));
        Assert.assertFalse(ConditionParser.checkCondition("defined(undef) OR defined(otherundef)", def));
        Assert.assertFalse(ConditionParser.checkCondition("defined(undef) And defined(otherundef)", def));
        Assert.assertFalse(ConditionParser.checkCondition("defined(def1) and defined(undef)", def));
        Assert.assertFalse(ConditionParser.checkCondition("DEFI(invalid", def));
    }

    @Test
    public void testParseComplex() {
        Set<String> def = new HashSet<>(Arrays.asList("DEF1", "DEF2", "DEF3", "DEF4"));
        Assert.assertTrue(ConditionParser.checkCondition("not (defined(undef))", def));
        Assert.assertTrue(ConditionParser.checkCondition("not (defined(undef)) and\n (defined(def1)\n or defined(undef))", def));
        Assert.assertTrue(ConditionParser.checkCondition("defined(def1) and defined(def2)", def));
        Assert.assertTrue(ConditionParser.checkCondition("defined(def1) and ( (defined(undef) or defined(def2)) or (not defined(undef) and defined(def3)) )", def));
    }
}
