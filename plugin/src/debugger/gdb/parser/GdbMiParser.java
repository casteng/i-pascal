package com.siberika.idea.pascal.debugger.gdb.parser;

import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 29/03/2017
 */
public class GdbMiParser {

    private final String input;
    private final int end;
    private int pos = 0;
    private char lastChar;

    public static final Pattern A = Pattern.compile("::\"-\\[(.*?)\\]\"");

    public GdbMiParser(@NotNull String input) {
        this.input = A.matcher(input).replaceAll("::'-[$1]'");
        this.end = input.length();
    }

    private GdbMiLine parseLine() {
        GdbMiLine result = new GdbMiLine(parseToken(), getType(lastChar), parseClass());
        parseValues(result.getResults());
        return result;
    }

    // ( "," result )*
    private void parseValues(GdbMiResults dest) {
        while (',' == lastChar) {
            parseResult(dest);
            nextChar();
        }
    }

    // variable "=" value
    private void parseResult(GdbMiResults dest) {
        String name = parseString();
        if (null == name) {
            return;
        }
        if ('=' == lastChar) {
            Object value = parseValueOrStop();
            dest.setValue(name, value);
        } else {
            throw new GdbMiParserException("Error parsing result", pos, input);
        }
    }

    private Object parseValueOrStop() {
        Object value = parseValue();
        if (value != null) {
            return value;
        } else {
            throw new GdbMiParserException("Error parsing value", pos, input);
        }
    }

    // const | tuple | list
    private Object parseValue() {
        switch (nextChar()) {
            case '"':
                return parseConst();
            case '{':
                return parseTuple();
            case '[':
                return parseList();
            default:
                return null;
        }
    }

    // "[]" | "[" value ( "," value )* "]" | "[" result ( "," result )* "]"
    private Object parseList() {
        Collection<Object> res = new SmartList<Object>();
        int savePos = pos;
        try {
            parseValuesList(res);
        } catch (GdbMiParserException e) {
            pos = savePos;
            parseResultsList(res);
        }
        if (lastChar != ']') {
            throw new GdbMiParserException("Error parsing list", pos, input);
        }
        return res;
    }

    // result ( "," result )*
    private void parseResultsList(Collection<Object> res) {
        parseResultToList(res);
        nextChar();
        while (',' == lastChar) {
            parseResultToList(res);
            nextChar();
        }
    }

    private void parseResultToList(Collection<Object> res) {
        GdbMiResults r = new GdbMiResults();
        parseResult(r);
        res.add(r);
    }

    // value ( "," value )*
    private void parseValuesList(Collection<Object> res) {
        Object value = parseValue();
        if (null == value) {
            // empty list
            if (']' == lastChar) {
                return;
            } else {
                throw new GdbMiParserException("Error parsing values list", pos, input);
            }
        }
        res.add(value);
        nextChar();
        while (',' == lastChar) {
            res.add(parseValueOrStop());
            nextChar();
        }
    }

    // "{}" | "{" result ( "," result )* "}"
    private Object parseTuple() {
        GdbMiResults res = new GdbMiResults();
        parseResult(res);
        // Empty tuple
        if ('}' == lastChar) {
            return res;
        }
        nextChar();
        parseValues(res);
        if ('}' != lastChar) {
            throw new GdbMiParserException("Error parsing tuple", pos, input);
        }
        return res;
    }

    //  c-string
    private Object parseConst() {
        StringBuilder t = new StringBuilder();
        while (nextChar() != 0 && lastChar != '"') {
            t = t.append(lastChar);
            if ('\\' == lastChar) {
                t = t.append(nextChar());
            }
        }
        if ('"' != lastChar) {
            throw new GdbMiParserException("Error parsing const", pos, input);
        }
        return t.toString();
    }

    private String parseClass() {
        return parseString();
    }

    private String parseString() {
        StringBuilder t = new StringBuilder();
        while ((nextChar() >= 'a') && (lastChar <= 'z') || ('-' == lastChar) || ('_' == lastChar)) {
            t = t.append(lastChar);
        }
        return t.length() > 0 ? t.toString() : null;
    }

    private GdbMiLine.Type getType(char typeChar) {
        switch (typeChar) {
            case '*':
                return GdbMiLine.Type.EXEC_ASYNC;
            case '+':
                return GdbMiLine.Type.STATUS_ASYNC;
            case '=':
                return GdbMiLine.Type.NOTIFY_ASYNC;
            case '^':
                return GdbMiLine.Type.RESULT_RECORD;
            case '~':
                return GdbMiLine.Type.CONSOLE_STREAM;
            case '@':
                return GdbMiLine.Type.TARGET_STREAM;
            case '&':
                return GdbMiLine.Type.LOG_STREAM;
        }
        return null;
    }

    private Long parseToken() {
        StringBuilder t = new StringBuilder();
        while (Character.isDigit(nextChar())) {
            t = t.append(lastChar);
        }
        return t.length() > 0 ? Long.parseLong(t.toString()) : null;
    }

    private char nextChar() {
        lastChar = pos < end ? input.charAt(pos++) : 0;
        return lastChar;
    }

    public static GdbMiLine parseLine(String line) {
        return new GdbMiParser(line).parseLine();
    }
/*
output →
    ( out-of-band-record )* [ result-record ] "(gdb)" nl

result-record →
    [ token ] "^" result-class ( "," result )* nl

out-of-band-record →
    async-record | stream-record

async-record →
    exec-async-output | status-async-output | notify-async-output

exec-async-output →
    [ token ] "*" async-output nl

status-async-output →
    [ token ] "+" async-output nl

notify-async-output →
    [ token ] "=" async-output nl

async-output →
    async-class ( "," result )*

result-class →
    "done" | "running" | "connected" | "error" | "exit"

async-class →
    "stopped" | others (where others will be added depending on the needs—this is still in development).

result →
    variable "=" value

variable →
    string

value →
    const | tuple | list

const →
    c-string

tuple →
    "{}" | "{" result ( "," result )* "}"

list →
    "[]" | "[" value ( "," value )* "]" | "[" result ( "," result )* "]"

stream-record →
    console-stream-output | target-stream-output | log-stream-output

console-stream-output →
    "~" c-string nl

target-stream-output →
    "@" c-string nl

log-stream-output →
    "&" c-string nl

nl →
    CR | CR-LF

token →
    any sequence of digits.
*/
}
