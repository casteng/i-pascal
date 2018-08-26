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
   op        ::= or | and
   cond      ::= [not] "defined(" ident ")"
 */
public class ConditionParser {
    private enum OPERATION {OR, AND}

    private static final Pattern COND = Pattern.compile("(?i)defined\\((\\w+)\\)");
    private static final Pattern NOT = Pattern.compile("(?i)not");
    private static final Pattern OP = Pattern.compile("(?i)or|and");

    static boolean checkCondition(String condition, Set<String> def) {
        if (null == condition) {
            return false;
        }
        Scanner scanner = new Scanner(condition);
        boolean res = parseCond(scanner, def, true);
        while (scanner.hasNext(OP)) {
            OPERATION op = parseOp(scanner);
            if (op == OPERATION.OR) {
                boolean res2 = parseCond(scanner, def, !res);
                res = res || res2;
            } else {
                boolean res2 = parseCond(scanner, def, res);
                res = res && res2;
            }
        }
        return res;
    }

    // cond ::= [not] "defined(" ident ")"
    private static boolean parseCond(Scanner scanner, Set<String> defs, boolean needEval) {
        boolean neg = scanner.hasNext(NOT) && (scanner.next(NOT) != null);
        String next = scanner.hasNext(COND) ? scanner.next(COND) : null;
        if ((next != null) && needEval) {
            MatchResult mr = scanner.match();
            return defs.contains(mr.group(1).toUpperCase()) ^ neg;
        }
        return false;
    }

    private static OPERATION parseOp(Scanner scanner) {
        String next = scanner.next(OP);
        return OPERATION.valueOf(next.toUpperCase());
    }
}
