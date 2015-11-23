package com.siberika.idea.pascal.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Author: George Bakhtadze
 * Date: 21/07/2015
 */
public class StrUtilTest {

    @Test
    public void testGetFieldName() throws Exception {
        Assert.assertEquals("", StrUtil.getFieldName(""));
        Assert.assertEquals("Method", StrUtil.getFieldName("Class.Method"));
        Assert.assertEquals("Method()", StrUtil.getFieldName("Class.Method()"));
        Assert.assertEquals("Method(param: System.Integer)", StrUtil.getFieldName("Class.Method(param: System.Integer)"));
        Assert.assertEquals("Method: Integer", StrUtil.getFieldName("Class.Method: Integer"));
        Assert.assertEquals("Method: System.Integer", StrUtil.getFieldName("Class.Method: System.Integer"));
        Assert.assertEquals("Method()", StrUtil.getFieldName("Class.SubClass.Method()"));
        Assert.assertEquals("Method(param: System.Integer): System.Integer", StrUtil.getFieldName("Class.SubClass.Method(param: System.Integer): System.Integer"));
        Assert.assertEquals("Method(System.Integer, Word): System.Integer", StrUtil.getFieldName("Class.SubClass.Method(System.Integer, Word): System.Integer"));
    }
}