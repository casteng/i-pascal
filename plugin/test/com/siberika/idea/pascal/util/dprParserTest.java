package com.siberika.idea.pascal.util;

import com.siberika.idea.pascal.module.DPRParser;
import com.siberika.idea.pascal.module.ProjectData;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class dprParserTest {

    @Test
    public void testGetFieldName() throws Exception {
        ProjectData res = DPRParser.parse(new File("testData/util/dprParser.dpr"));
        Assert.assertEquals("dprParser.dpr", res.getMainFile());
        Assert.assertEquals(11, res.getUnits().size());
        Assert.assertEquals("dprParser.dpr", res.getUnits().get(0));
        for (int i = 1; i <= 10; i++) {
            System.out.println(res.getUnits().get(i));
            Assert.assertTrue(res.getUnits().get(i).endsWith("unit" + i + ".pas"));
        }
    }

}