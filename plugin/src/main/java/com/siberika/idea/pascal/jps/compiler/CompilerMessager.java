package com.siberika.idea.pascal.jps.compiler;

/**
 * Author: George Bakhtadze
 * Date: 09/05/2014
 */
public interface CompilerMessager {
    CompilerMessager NO_OP_MESSAGER = new CompilerMessager() {
        @Override
        public void hint(String msgId, String msg, String path, long line, long column) {
        }

        @Override
        public void info(String msgId, String msg, String path, long line, long column) {
        }

        @Override
        public void warning(String msgId, String msg, String path, long line, long column) {
        }

        @Override
        public void error(String msgId, String msg, String path, long line, long column) {
        }
    };

    void hint(String msgId, String msg, String path, long line, long column);
    void info(String msgId, String msg, String path, long line, long column);
    void warning(String msgId, String msg, String path, long line, long column);
    void error(String msgId, String msg, String path, long line, long column);
}
