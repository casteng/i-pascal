package com.siberika.idea.pascal;

import org.junit.Test;

/**
 * Author: George Bakhtadze
 * Date: 20/03/2013
 */
public class Test1 {
    @Test
    public void test() throws Exception {
        int a = 100;
        int b = a * 1000000;
        System.out.println(log2(a));
        System.out.println(log2(b));
        System.out.println(log2(b)/log2(a));
    }

    private double log2(int i) {
        return Math.log(i) / Math.log(2);
    }


}
