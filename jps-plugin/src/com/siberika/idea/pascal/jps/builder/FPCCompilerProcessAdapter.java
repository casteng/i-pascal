package com.siberika.idea.pascal.jps.builder;

import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import org.jetbrains.annotations.NonNls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 12/05/2014
 */
public class FPCCompilerProcessAdapter extends PascalCompilerProcessAdapter {

    @NonNls private static final String PATTERN_COMMON = "((.*)\\((\\d+)(,(\\d+))?\\))?\\s*";
    @NonNls private static final String PATTERN_MESSAGE = "\\s*\\((\\d+)\\) (.*)$";
    @NonNls private static final Pattern PATTERN_ERROR = Pattern.compile("((.*)\\((\\d+)(,(\\d+))?\\))?\\s*(Error|Fatal):" + PATTERN_MESSAGE);
    @NonNls private static final Pattern PATTERN_WARNING = Pattern.compile(PATTERN_COMMON + "(Error|Fatal|Warning|warning):" + PATTERN_MESSAGE);
    @NonNls private static final Pattern PATTERN_INFO = Pattern.compile(PATTERN_COMMON + "(Note|Hint|)?:?" + PATTERN_MESSAGE);

    public FPCCompilerProcessAdapter(CompilerMessager messager) {
        super(messager);
    }

    @Override
    protected boolean processLine(CompilerMessager messager, String line) {
        if (line == null) {
            return false;
        }
        Matcher matcher = PATTERN_ERROR.matcher(line);
        if (matcher.find()) {
            PascalCompilerMessager.createMessage(CompilerMessageCategory.ERROR, line, matcher, messager);
        } else {
            matcher = PATTERN_WARNING.matcher(line);
            if (matcher.find()) {
                PascalCompilerMessager.createMessage(CompilerMessageCategory.WARNING, line, matcher, messager);
            } else {
                matcher = PATTERN_INFO.matcher(line);
                if (matcher.find()) {
                    PascalCompilerMessager.createMessage(CompilerMessageCategory.HINT, line, matcher, messager);
                } else {
                    PascalCompilerMessager.createMessage(CompilerMessageCategory.INFO, line, null, messager);
                }
            }
        }
        return true;
    }

}
