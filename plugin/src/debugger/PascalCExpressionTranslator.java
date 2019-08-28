package com.siberika.idea.pascal.debugger;

import java.util.HashMap;
import java.util.Map;

public class PascalCExpressionTranslator {

    private static final Map<String, String> TRANSLATION_MAP = getTMap();

    private static Map<String, String> getTMap() {
        Map<String, String> res = new HashMap<>();
        res.put("(?i)\\bself\\b", "this");
        res.put("(?i)\\b([a-z0-9._]+)\\^", "\\(*$1\\)");
        res.put("\\$([a-fA-F0-9]+)", "0x$1");
        res.put("@", "&");
        res.put("(?i)\\bmod\\b", "%");
        res.put("(?i)\\bdiv\\b", "/");
        res.put("<>", "!=");
        res.put("(?i)\\bnot\\b", "!");
        res.put("(?i)\\band\\b", "&&");
        res.put("(?i)\\bor\\b", "||");
        res.put("(?i)\\bxor\\b", "^");
        return res;
    }

    public String translate(String expression) {
        expression = expression.toUpperCase();
        for (Map.Entry<String, String> entry : TRANSLATION_MAP.entrySet()) {
            expression = expression.replaceAll(entry.getKey(), entry.getValue());
        }
        return expression;
    }

    public String reverse(String value) {
        return value;
    }
}
