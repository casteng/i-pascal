package com.siberika.idea.pascal.lang.compiled;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Author: George Bakhtadze
 * Date: 01/12/2013
 */
public class PPUDecompilerTest {
    @Test
    public void testGetPPUDumpVersion() throws Exception {
        File ppuDump = new File("/usr/lib/codetyphon/fpc/bin/i386-linux/ppudump");
        String ver = PPUDecompilerCache.getPPUDumpVersion(ppuDump);
        assertThat(ver, is(PPUDecompilerCache.PPUDUMP_VERSION_MIN));
        assertThat(ver.compareTo("2.6.1") >= 0, is(true));
        assertThat(ver.compareTo("2.8.1") < 0, is(true));
    }
}
