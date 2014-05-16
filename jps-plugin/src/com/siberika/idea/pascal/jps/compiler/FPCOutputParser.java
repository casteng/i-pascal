package com.siberika.idea.pascal.jps.compiler;

import com.intellij.compiler.OutputParser;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.NonNls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 1/6/13
 */
public class FPCOutputParser extends OutputParser {

    public static final Logger LOG = Logger.getInstance(FPCOutputParser.class.getName());

    @NonNls private static final String PATTERN_COMMON = "((.*)\\((\\d+),(\\d+)\\))?\\s*";
    @NonNls private static final String PATTERN_MESSAGE = "\\s*(.*)$";
    @NonNls private static final Pattern PATTERN_ERROR = Pattern.compile(PATTERN_COMMON + "(Error:|Fatal:)" + PATTERN_MESSAGE);
    @NonNls private static final Pattern PATTERN_WARNING = Pattern.compile(PATTERN_COMMON + "(Warning:|warning:)" + PATTERN_MESSAGE);
    @NonNls private static final Pattern PATTERN_INFO = Pattern.compile(PATTERN_COMMON + PATTERN_MESSAGE);

    @Override
    public boolean processMessageLine(Callback callback) {
        final String line = callback.getNextLine();

        if (LOG.isDebugEnabled()) {
            LOG.debug(line);
        }

        return processLine(null, line);
    }

    public static boolean processLine(CompilerMessager messager, String line) {
        if (line == null) {
            return false;
        }

        Matcher matcher = PATTERN_ERROR.matcher(line);
        if (matcher.find()) {
            createMessage(CompilerMessageCategory.ERROR, line, matcher, messager);
        } else {
            matcher = PATTERN_WARNING.matcher(line);
            if (matcher.find()) {
                createMessage(CompilerMessageCategory.WARNING, line, matcher, messager);
            } else {
                matcher = PATTERN_INFO.matcher(line);
                if (matcher.find()) {
                    createMessage(CompilerMessageCategory.INFORMATION, line, matcher, messager);
                } else {
                    createMessage(CompilerMessageCategory.INFORMATION, line, null, messager);
                }
            }
        }

        return true;
    }

    private static void createMessage(CompilerMessageCategory category, String line, Matcher matcher, CompilerMessager messager) {
        int lineNum = -1;
        int colNum = -1;
        String message = null;
        String url = "";
        if (null != matcher) {
            int groupCount = matcher.groupCount();
            if (groupCount >= 4) {
                url = matcher.group(2);
                if (url != null) {
                    url = VirtualFileManager.extractPath(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, url));
                }
                try {
                    lineNum = Integer.valueOf(matcher.group(3));
                    colNum = Integer.valueOf(matcher.group(4));
                } catch (NumberFormatException ignore) {}
            }
            message = matcher.group(groupCount);
        }
        if (null == message) message = line;

        if (CompilerMessageCategory.ERROR.equals(category)) {
            messager.error(message, url, lineNum, colNum);
        } else if (CompilerMessageCategory.WARNING.equals(category)) {
            messager.warning(message, url, lineNum, colNum);
        } else {
            messager.info(message, url, lineNum, colNum);
        }
    }

    public boolean isTrimLines() {
        return false;
    }

}
