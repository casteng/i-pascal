package com.siberika.idea.pascal.lang.psi.impl;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class RoutineUtilTest {

    @Test
    public void testParseTypeParameters() {
        Assert.assertEquals(Arrays.asList("T"), RoutineUtil.parseTypeParametersStr("<T>"));
        Assert.assertEquals(Arrays.asList("T", "P"), RoutineUtil.parseTypeParametersStr("<T, P>"));
        Assert.assertEquals(Arrays.asList("T"), RoutineUtil.parseTypeParametersStr("<T: class>"));
        Assert.assertEquals(Arrays.asList("T", "P"), RoutineUtil.parseTypeParametersStr("<T, P: record>"));
        Assert.assertEquals(Arrays.asList("T", "P"), RoutineUtil.parseTypeParametersStr("<T:constructor;P: record>"));
    }

}