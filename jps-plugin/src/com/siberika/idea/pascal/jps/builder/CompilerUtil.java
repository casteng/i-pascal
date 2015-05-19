package com.siberika.idea.pascal.jps.builder;

import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

/**
 * Author: George Bakhtadze
 * Date: 19/05/2015
 */
public class CompilerUtil {
    static class PascalCompilerMessager implements CompilerMessager {
        private final CompileContext context;
        private final String name;

        public PascalCompilerMessager(String name, CompileContext context) {
            this.name = name;
            this.context = context;
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
}
