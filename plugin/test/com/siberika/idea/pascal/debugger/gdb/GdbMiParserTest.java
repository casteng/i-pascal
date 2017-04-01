package com.siberika.idea.pascal.debugger.gdb;

import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiParser;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: George Bakhtadze
 * Date: 29/03/2017
 */
public class GdbMiParserTest {

    @Test
    public void testParseListResults() throws Exception {
        GdbMiLine res = GdbMiParser.parseLine("^done,stack=[disp=\"keep\",bkptno=\"1\",frame={}]");
        assertEquals(GdbMiLine.Type.RESULT_RECORD, res.getType());
        GdbMiResults r = (GdbMiResults) res.getResults().getList("stack").get(0);
        assertEquals("keep", r.getValue("disp"));
    }

    @Test
    public void testParseListValues() throws Exception {
        GdbMiLine res = GdbMiParser.parseLine("^done,stack=[\"value1\",[],{}]");
        assertEquals(GdbMiLine.Type.RESULT_RECORD, res.getType());
    }

    @Test
    public void testParseLine() throws Exception {
        GdbMiLine res = GdbMiParser.parseLine("123*stopped,reason=\"breakpoint-hit\",disp=\"keep\",bkptno=\"1\",frame={addr=\"0x00000000004257f1\",func=\"main\",args=[{name=\"APPLICATION\",value=\"0x7ffff7fbc040\"}],file=\"test.pas\",fullname=\"~/src/test/test.pas\",line=\"81\"},thread-id=\"1\",stopped-threads=\"all\",core=\"2\"");
        assertEquals(Long.valueOf(123), res.getToken());
        assertEquals(GdbMiLine.Type.EXEC_ASYNC, res.getType());
        assertEquals("stopped", res.getRecClass());
        assertEquals("breakpoint-hit", res.getResults().getValue("reason"));
        assertEquals("all", res.getResults().getValue("stopped-threads"));
        GdbMiResults frame = (GdbMiResults) res.getResults().getValue("frame");
        assertEquals("~/src/test/test.pas", frame.getValue("fullname"));
        frame.getValue("args");
    }

    @Test
    public void testStop() throws Exception {
        String s = "*stopped,reason=\"signal-received\",signal-name=\"SIGINT\",signal-meaning=\"Interrupt\",frame={addr=\"0x0000000000401434\",func=\"main\",args=[{name=\"SYSNR\",value=\"140737488346656\"},{name=\"PARAM1\",value=\"140737488346672\"},{name=\"PARAM2\",value=\"140737488346672\"}],file=\"x86_64/syscall.inc\",fullname=\"~/src/test/x86_64/syscall.inc\",line=\"81\"},thread-id=\"1\",stopped-threads=\"all\",core=\"1\"\n";
        GdbMiLine res = GdbMiParser.parseLine(s);
        assertEquals(GdbMiLine.Type.EXEC_ASYNC, res.getType());
        assertEquals("stopped", res.getRecClass());
        assertEquals("signal-received", res.getResults().getValue("reason"));
        assertEquals("SIGINT", res.getResults().getValue("signal-name"));
        assertEquals("Interrupt", res.getResults().getValue("signal-meaning"));
    }

}