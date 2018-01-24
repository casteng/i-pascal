package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class PPUDumpParserTest {

    private Map<String, File> files = new HashMap<String, File>();

    protected String getTestDataPath() {
        return "testData/gotoSymbol";
    }

    @Before
    public void setUp() {
        files.put("system", new File("~/src/test/system.xml"));
        files.put("ctypes", new File("~/src/test/ctypes.xml"));
    }

    @Ignore
    @Test
    public void test1() throws IOException, ParserConfigurationException, SAXException, ParseException {
        PPUDecompilerCache cache = new PPUDecompilerCacheTest(mock(Module.class), files);
        PPUDumpParser.Section section1 = PPUDumpParser.parse(new FileInputStream(files.get("system")), cache);
        PPUDumpParser.Section section2 = PPUDumpParser.parse(new FileInputStream(files.get("ctypes")), cache);
        System.out.println(section2);
    }

    private static class PPUDecompilerCacheTest extends PPUDecompilerCache {
        private final Map<String, File> files;

        public PPUDecompilerCacheTest(Module module, Map<String, File> files) {
            super(module);
            this.files = files;
        }

        @Override
        String retrieveXml(String key, File ppuDump) throws IOException {
            return IOUtils.toString(new FileInputStream(files.get(key)), "UTF-8");
        }

        @Override
        File retrievePpuDump(String key) {
            return null;
        }

        @Override
        VirtualFile retrieveFile(Module module, String unitName) {
            return new LightVirtualFile(unitName);
        }
    }
}