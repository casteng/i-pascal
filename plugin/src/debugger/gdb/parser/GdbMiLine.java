package com.siberika.idea.pascal.debugger.gdb.parser;

/**
 * Author: George Bakhtadze
 * Date: 29/03/2017
 */
public class GdbMiLine {
    private final Long token;
    private final Type type;
    private final String recClass;
    private final GdbMiResults results = new GdbMiResults();

    public GdbMiLine(Long token, Type type, String recClass) {
        this.token = token;
        this.type = type;
        this.recClass = recClass;
    }

    public Long getToken() {
        return token;
    }

    public Type getType() {
        return type;
    }

    public String getRecClass() {
        return recClass;
    }

    public GdbMiResults getResults() {
        return results;
    }

    public enum Type {
        EXEC_ASYNC,
        STATUS_ASYNC,
        NOTIFY_ASYNC,
        CONSOLE_STREAM,
        TARGET_STREAM,
        LOG_STREAM,
        RESULT_RECORD
    }
}
