package com.siberika.idea.pascal.lang.compiled;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Author: George Bakhtadze
 * Date: 01/12/2013
 */
public class PPUDecompilerTest {
    @Test
    public void testGetPPUDumpVersion() throws Exception {
        File ppuDump = new File("/usr/lib/codetyphon/fpc/bin/i386-linux/ppudump");
        String ver = PPUDecompilerCache.getPPUDumpVersion(ppuDump);
        assertEquals(ver, PPUDecompilerCache.PPUDUMP_VERSION_MIN);
        assertTrue(ver.compareTo("2.6.1") >= 0);
        assertTrue(ver.compareTo("2.8.1") < 0);
    }
}
