package com.siberika.idea.pascal.lang.lexer;

import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 25/08/2018
 *
 * condition ::= cond {op cond}
 * op        ::= or | and
 * cond      ::= [not] ("(" condition ")") | ("defined(" ident ")")
 */
class ConditionParser {
    private enum OPERATION {OR, AND}

    private static final Pattern COND = Pattern.compile("(?i)defined\\((\\w+)\\)");
    private static final Pattern COND_PREPARED = Pattern.compile("_\\[(\\w+)]");
    private static final Pattern NOT = Pattern.compile("(?i)not");
    private static final Pattern OP = Pattern.compile("(?i)or|and");

    private static final Pattern PAREN_OPEN = Pattern.compile("\\(");
    private static final Pattern PAREN_CLOSE = Pattern.compile("\\)");

    static boolean checkCondition(String condition, Set<String> defines) {
        if (null == condition) {
            return false;
        }
        ParserState state = new ParserState(COND.matcher(condition).replaceAll("_[$1]")
                .replaceAll("\\(", " ( ").replaceAll("\\)", " ) "), defines);
        Scanner scanner = new Scanner(state.condition);
        return parseCondition(scanner, state);
    }

    // condition ::= cond {op cond}
    private static boolean parseCondition(Scanner scanner, ParserState state) {
        boolean res = parseCond(scanner, state, true);
        while (scanner.hasNext(OP)) {
            OPERATION op = parseOp(scanner);
            if (op == OPERATION.OR) {
                boolean res2 = parseCond(scanner, state, !res);
                res = res || res2;
            } else {
                boolean res2 = parseCond(scanner, state, res);
                res = res && res2;
            }
        }
        return res;
    }

    // cond ::= [not] ("(" condition ")") | ("defined(" ident ")")
    private static boolean parseCond(Scanner scanner, ParserState state, boolean needEval) {
        boolean neg = scanner.hasNext(NOT) && (scanner.next(NOT) != null);
        if (scanner.hasNext(PAREN_OPEN) && (scanner.next(PAREN_OPEN) != null)) {
            boolean result = parseCondition(scanner, state);
            if (scanner.hasNext(PAREN_CLOSE) && (scanner.next(PAREN_CLOSE) != null)) {
                return result ^ neg;
            } else {
                return false;
            }
        }
        String next = scanner.hasNext(COND_PREPARED) ? scanner.next(COND_PREPARED) : null;
        if ((next != null) && needEval) {
            MatchResult mr = scanner.match();
            return state.defines.contains(mr.group(1).toUpperCase()) ^ neg;
        }
        return false;
    }

    private static OPERATION parseOp(Scanner scanner) {
        String next = scanner.next(OP);
        return OPERATION.valueOf(next.toUpperCase());
    }

    private static class ParserState {
        private final String condition;
        private final Set<String> defines;

        private ParserState(String condition, Set<String> defines) {
            this.condition = condition;
            this.defines = defines;
        }
    }
}
