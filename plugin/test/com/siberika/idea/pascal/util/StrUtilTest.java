package com.siberika.idea.pascal.util;

import com.google.common.collect.ImmutableMap;
import com.intellij.codeInsight.template.impl.TemplateImpl;
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

    @Test
    public void testGetIncludeName() throws Exception {
        Assert.assertEquals(null, StrUtil.getIncludeName("bad name"));
        Assert.assertEquals("file", StrUtil.getIncludeName("{$I file}"));
        Assert.assertEquals("filename", StrUtil.getIncludeName("{$i filename}"));
        Assert.assertEquals("file", StrUtil.getIncludeName("{$Include file}"));
        Assert.assertEquals("file", StrUtil.getIncludeName("{$Include  file}"));
        Assert.assertEquals("file", StrUtil.getIncludeName("{$i    file   }"));
        Assert.assertEquals("filename", StrUtil.getIncludeName("{$inCluDe filename}"));
        Assert.assertEquals("filename", StrUtil.getIncludeName("{$inCluDe 'filename'}"));
        Assert.assertEquals("file name", StrUtil.getIncludeName("{$inCluDe 'file name'}"));
        Assert.assertEquals(" file name ", StrUtil.getIncludeName("{$inCluDe  ' file name ' }"));
        Assert.assertEquals(null, StrUtil.getIncludeName("{$inCluDe ''}"));
    }

    @Test
    public void testTemplate() throws Exception {
        TemplateImpl t = (TemplateImpl) DocUtil.createTemplate("var\ni: $TYPE$", ImmutableMap.<String, String>of("TYPE", "T"), true);
        Assert.assertEquals(1, t.getVariableCount());
        Assert.assertEquals(1, t.getSegmentsCount());
        Assert.assertEquals("TYPE", t.getVariableNameAt(0));
        Assert.assertEquals("T", t.getDefaultValueAt(0).calculateResult(null).toString());
    }
}