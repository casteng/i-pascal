package com.siberika.idea.pascal.debugger.gdb;

import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiParser;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import org.junit.Test;

import java.util.List;

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
    @Test
    public void testBreakpointInsert() throws Exception {
        String s = "^done,bkpt={number=\"5\",type=\"breakpoint\",disp=\"keep\",enabled=\"y\",addr=\"0x000000000046a15b\",func=\"GETATTRIBUTEDATASIZE\",file=\"test.pas\",fullname=\"~/src/test/test.pas\",line=\"608\",thread-groups=[\"i1\"],times=\"0\",original-location=\"~/src/test/test.pas:608\"}";
        GdbMiLine res = GdbMiParser.parseLine(s);
        assertEquals(GdbMiLine.Type.RESULT_RECORD, res.getType());
        GdbMiResults bp = res.getResults().getTuple("bkpt");
        assertEquals(Integer.valueOf(5), bp.getInteger("number"));
        assertEquals("breakpoint", bp.getString("type"));
        assertEquals("608", bp.getValue("line"));
    }


    @Test
    public void testBreakpointInsert2() throws Exception {
        String s = "^done,bkpt={number=\"1\",type=\"breakpoint\",disp=\"keep\",enabled=\"y\",addr=\"0x0000000000011c40\",func=\"::\"-[TMyWindow update]\"(SEL)\",file=\"test.pas\",fullname=\"~/src/test/test.pas\",line=\"243\",pending=[\"test.pas:243\"],times=\"0\",original-location=\"test.pas:243\"}";
        GdbMiLine res = GdbMiParser.parseLine(s);
        assertEquals(GdbMiLine.Type.RESULT_RECORD, res.getType());
        GdbMiResults bp = res.getResults().getTuple("bkpt");
        assertEquals(Integer.valueOf(1), bp.getInteger("number"));
        assertEquals("breakpoint", bp.getString("type"));
        assertEquals("::'-[TMyWindow update]'(SEL)", bp.getString("func"));
        assertEquals("243", bp.getValue("line"));
    }

    @Test
    public void testStringWithEscapedQuotes() throws Exception {
        String s = "^error,msg=\"No symbol table is loaded.  Use the \\\"file\\\" command.\"";
        GdbMiLine res = GdbMiParser.parseLine(s);
        assertEquals(GdbMiLine.Type.RESULT_RECORD, res.getType());
        assertEquals("No symbol table is loaded.  Use the \\\"file\\\" command.", res.getResults().getString("msg"));
    }

    @Test
    public void testVar() throws Exception {
        String s = "^done,name=\"test\",numchild=\"2\",value=\"{...}\",type=\"number\",has_more=\"0\"";
        GdbMiLine res = GdbMiParser.parseLine(s);
        assertEquals(GdbMiLine.Type.RESULT_RECORD, res.getType());
        assertEquals("test", res.getResults().getString("name"));
        assertEquals(Integer.valueOf(2), res.getResults().getInteger("numchild"));
    }

    @Test
    public void testVarUpdate() throws Exception {
        String s = "^done,changelist=[{name=\"test\",value=\"0x7ffff7f843e8\",in_scope=\"true\",type_changed=\"false\",has_more=\"0\"}]";
        GdbMiLine res = GdbMiParser.parseLine(s);
        List<Object> changes = res.getResults().getList("changelist");
        GdbMiResults change = (GdbMiResults) changes.get(0);
        assertEquals(GdbMiLine.Type.RESULT_RECORD, res.getType());
        assertEquals("test", change.getString("name"));
        assertEquals("0x7ffff7f843e8", change.getString("value"));
        assertEquals("false", change.getString("type_changed"));
        assertEquals(Integer.valueOf(0), change.getInteger("has_more"));
    }

    @Test
    public void testChildren() throws Exception {
        String s = "^done,numchild=\"11\",children=[child={name=\"app.TOBJECT\",exp=\"TOBJECT\",numchild=\"1\",value=\"{...}\",type=\"TOBJECT\"},child={name=\"app.FCONFIG\",exp=\"FCONFIG\",numchild=\"2\",value=\"0x7ffff7fc4180\",type=\"TCECONFIG\"},child={name=\"app.FACTIVE\",exp=\"FACTIVE\",numchild=\"0\",value=\"false\",type=\"BOOLEAN\"},child={name=\"app.FNAME\",exp=\"FNAME\",numchild=\"1\",value=\"0x7ffff7fd6418 'TileDemo'\",type=\"UNICODESTRING\"},child={name=\"app.FTERMINATED\",exp=\"FTERMINATED\",numchild=\"0\",value=\"false\",type=\"BOOLEAN\"},child={name=\"app.FMESSAGEHANDLER\",exp=\"FMESSAGEHANDLER\",numchild=\"2\",value=\"{...}\",type=\"TCEMESSAGEHANDLER\"},child={name=\"app.NAME\",exp=\"NAME\",numchild=\"1\",value=\"0x7ffff7fd6418 'TileDemo'\",type=\"UNICODESTRING\"},child={name=\"app.TERMINATED\",exp=\"TERMINATED\",numchild=\"0\",value=\"false\",type=\"BOOLEAN\"},child={name=\"app.ACTIVE\",exp=\"ACTIVE\",numchild=\"0\",value=\"false\",type=\"BOOLEAN\"},child={name=\"app.CFG\",exp=\"CFG\",numchild=\"2\",value=\"0x7ffff7fc4180\",type=\"TCECONFIG\"},child={name=\"app.MESSAGEHANDLER\",exp=\"MESSAGEHANDLER\",numchild=\"2\",value=\"{...}\",type=\"TCEMESSAGEHANDLER\"}],has_more=\"0\"\n";
        GdbMiLine res = GdbMiParser.parseLine(s);
        assertEquals(Integer.valueOf(11), res.getResults().getInteger("numchild"));
        List<Object> children = res.getResults().getList("children");
        GdbMiResults childRes = (GdbMiResults) children.get(0);
        GdbMiResults child = childRes.getTuple("child");
        assertEquals("app.TOBJECT", child.getString("name"));
        assertEquals(Integer.valueOf(1), child.getInteger("numchild"));
    }
}