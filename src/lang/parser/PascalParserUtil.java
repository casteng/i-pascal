package com.siberika.idea.pascal.lang.parser;

import com.intellij.lang.PsiBuilder;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalParserUtil extends GeneratedParserUtilBase {
    public static boolean parsePascal(PsiBuilder builder_, int level, Parser parser) {
        ErrorState state = ErrorState.get(builder_);
        return parseAsTree(state, builder_, level, DUMMY_BLOCK, true, parser, TRUE_CONDITION);
    }
}
