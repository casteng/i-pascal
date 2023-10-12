package com.siberika.idea.pascal.debugger.gdb.parser;

/**
 * Author: George Bakhtadze
 * Date: 30/03/2017
 */
public class GdbMiParserException extends RuntimeException {
    private final int pos;
    private final String input;

    public GdbMiParserException(String msg, int pos, String input) {
        super(String.format("%s at %d, \"%s\"", msg, pos, input.substring(pos)));
        this.pos = pos;
        this.input = input;
    }

    public int getPos() {
        return pos;
    }

    public String getInput() {
        return input;
    }
}
