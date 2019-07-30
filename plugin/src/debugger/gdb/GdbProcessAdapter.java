package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.openapi.diagnostic.Logger;
import com.siberika.idea.pascal.debugger.CommandSender;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiParser;
import com.siberika.idea.pascal.jps.util.PascalConsoleProcessAdapter;
import com.siberika.idea.pascal.util.StrUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 28/03/2017
 */
public class GdbProcessAdapter extends PascalConsoleProcessAdapter {
    private static final Logger LOG = Logger.getInstance(GdbProcessAdapter.class);

    private static final Pattern PATTERN_LLDB_FRAME_VAR = Pattern.compile("~?\"?(\\(.+\\)) [^=]+ = (.*)\\$(.*)(\\\\n)?\"?\\s*");

    private final PascalXDebugProcess process;

    public GdbProcessAdapter(PascalXDebugProcess xDebugProcess) {
        this.process = xDebugProcess;
    }

    @Override
    public boolean onLine(String text) {
        try {
            GdbMiLine res = GdbMiParser.parseLine(text);
            if (res.getType() == GdbMiLine.Type.CONSOLE_STREAM) {             // Not parsed, try other options
                res = parseLLDBFrameVar(text, res);
            }
            final CommandSender.FinishCallback callback = process.findCallback(res);
            if (callback != null) {
                callback.call(res);
                return true;
            } else {
                process.handleResponse(res);
            }
        } catch (Exception e) {
            LOG.info("DBG Error: error handling input line: " + text);
        }
        return true;
    }

    // handling of LLDB fr v
    private GdbMiLine parseLLDBFrameVar(String text, GdbMiLine res) {
        Matcher m = PATTERN_LLDB_FRAME_VAR.matcher(text);
        if (m.matches()) {
            res = new GdbMiLine(res.getToken(), GdbMiLine.Type.RESULT_RECORD, "done");
            res.getResults().setValue("name", m.group(2));
            res.getResults().setValue("type", m.group(1));
            String value = StrUtil.removeSuffix(m.group(3), "\"");
            value = StrUtil.removeSuffix(value, "\\n");
            res.getResults().setValue("value", value);
        }
        return res;
    }

}
