package com.siberika.idea.pascal.jps.compiler;

/**
 * Author: George Bakhtadze
 * Date: 09/05/2014
 */
public interface CompilerMessager {
    void info(String msg, String path, long line, long column);
    void warning(String msg, String path, long line, long column);
    void error(String msg, String path, long line, long column);
}
