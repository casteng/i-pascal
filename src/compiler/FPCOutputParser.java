package com.siberika.idea.pascal.compiler;

import com.intellij.compiler.OutputParser;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NonNls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 1/6/13
 */
class FPCOutputParser extends OutputParser {

    public static final Logger LOG = Logger.getInstance(FPCOutputParser.class.getName());

    @NonNls private static final String PATTERN_COMMON = "((.*)\\((\\d+),(\\d+)\\))?\\s*";
    @NonNls private static final String PATTERN_MESSAGE = "\\s*(.*)$";
    @NonNls private static final Pattern PATTERN_ERROR = Pattern.compile(PATTERN_COMMON + "(Error:|Fatal:)" + PATTERN_MESSAGE);
    @NonNls private static final Pattern PATTERN_WARNING = Pattern.compile(PATTERN_COMMON + "(Warning:|warning:)" + PATTERN_MESSAGE);
    @NonNls private static final Pattern PATTERN_INFO = Pattern.compile(PATTERN_COMMON + PATTERN_MESSAGE);

    // /home/me/IdeaProjects/untitled1/untitled/src/test.pas(5,4) Fatal: Syntax error, "." expected but "end of file" found
    // Fatal: Can't open file "q"
    // /usr/bin/ld: warning: link.res contains output sections; did you forget -T?
    // /home/me/IdeaProjects/untitled1/untitled/src/test.pas(4,12) Warning: Variable "b" does not seem to be initialized


    @Override
    public boolean processMessageLine(Callback callback) {

        final String line = callback.getNextLine();

        if (LOG.isDebugEnabled()) {
            LOG.debug(line);
        }

        if (line == null) {
            return false;
        }

        System.out.println("===*** output line: " + line);

        //TODO: handle missing/wrong compiler executable case

        Matcher matcher = PATTERN_ERROR.matcher(line);
        if (matcher.find()) {
            createMessage(CompilerMessageCategory.ERROR, line, matcher, callback);
        } else {
            matcher = PATTERN_WARNING.matcher(line);
            if (matcher.find()) {
                createMessage(CompilerMessageCategory.WARNING, line, matcher, callback);
            } else {
                matcher = PATTERN_INFO.matcher(line);
                if (matcher.find()) {
                    createMessage(CompilerMessageCategory.INFORMATION, line, matcher, callback);
                } else {
                    createMessage(CompilerMessageCategory.INFORMATION, line, null, callback);
                }
            }
        }

        return true;
    }

    private void createMessage(CompilerMessageCategory category, String line, Matcher matcher, Callback callback) {
        int lineNum = -1;
        int colNum = -1;
        String message = null;
        String url = "";
        if (null != matcher) {
            int groupCount = matcher.groupCount();
            System.out.println("===*** groups: 3: " + matcher.group(3) + ", 4: " + matcher.group(4));
            if (groupCount >= 4) {
                url = matcher.group(2);
                if (url != null) {
                    url = VirtualFileManager.constructUrl("file", url);
                }
                try {
                    lineNum = Integer.valueOf(matcher.group(3));
                    colNum = Integer.valueOf(matcher.group(4));
                } catch (NumberFormatException ignore) {}
            }
            message = matcher.group(groupCount);
        }
        if (null == message) message = line;
        callback.message(category, message, url, lineNum, colNum);
    }

    public boolean isTrimLines() {
        return false;
    }

}
