package com.siberika.idea.pascal.lang.compiled;

import junit.framework.TestCase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DCUFileDecompilerTest extends TestCase {

    private static final Pattern ROUTINE = Pattern.compile("(\\s*)(procedure|function|operator)(\\s+)(@)(\\w+)");

    public void testRoutine() throws Exception {
        String line = "  procedure @test;";
        StringBuffer sb = new StringBuffer();
        Matcher m = ROUTINE.matcher(line);
        if (m.find()) {
            m.appendReplacement(sb, "$1$2$3$5");
        }
        m.appendTail(sb);
        System.out.println(sb.toString());
    }
}