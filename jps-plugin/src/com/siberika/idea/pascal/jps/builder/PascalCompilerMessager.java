package com.siberika.idea.pascal.jps.builder;

import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.io.URLUtil;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.util.regex.Matcher;

/**
 * Author: George Bakhtadze
 * Date: 20/05/2015
 */
class PascalCompilerMessager implements CompilerMessager {
    private final CompileContext context;
    private final String name;

    public PascalCompilerMessager(String name, CompileContext context) {
        this.name = name;
        this.context = context;
    }

    static void createMessage(CompilerMessageCategory category, String line, Matcher matcher, CompilerMessager messager) {
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
                    lineNum = Integer.parseInt(matcher.group(3));
                    colNum = Integer.parseInt(matcher.group(4));
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

    @Override
    public void info(String msg, String path, long line, long column) {
        context.processMessage(new CompilerMessage(name, BuildMessage.Kind.INFO, msg, path, -1l, -1l, -1l, line, column));
    }

    @Override
    public void warning(String msg, String path, long line, long column) {
        context.processMessage(new CompilerMessage(name, BuildMessage.Kind.WARNING, msg, path, -1l, -1l, -1l, line, column));
    }

    @Override
    public void error(String msg, String path, long line, long column) {
        context.processMessage(new CompilerMessage(name, BuildMessage.Kind.ERROR, msg, path, -1l, -1l, -1l, line, column));
    }
}
