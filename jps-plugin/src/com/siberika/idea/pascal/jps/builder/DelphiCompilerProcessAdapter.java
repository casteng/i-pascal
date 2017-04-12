package com.siberika.idea.pascal.jps.builder;

import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import com.siberika.idea.pascal.jps.compiler.DelphiBackendCompiler;
import org.jetbrains.annotations.NonNls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 12/05/2014
 */
public class DelphiCompilerProcessAdapter extends PascalCompilerProcessAdapter {

    @NonNls private static final String PATTERN_COMMON = "((.*)\\((\\d+)\\))?\\s*";
    @NonNls private static final String PATTERN_MESSAGE = "\\s*(.*)$";
    @NonNls private static final Pattern PATTERN_ERROR = Pattern.compile("((.*)\\((\\d+)\\))?\\s*(Error|Fatal):" + PATTERN_MESSAGE);
    @NonNls private static final Pattern PATTERN_WARNING = Pattern.compile(PATTERN_COMMON + "(Error|Fatal|Warning|warning):" + PATTERN_MESSAGE);
    @NonNls private static final Pattern PATTERN_INFO = Pattern.compile(PATTERN_COMMON + "(Hint:)" + PATTERN_MESSAGE);

    public DelphiCompilerProcessAdapter(CompilerMessager messager) {
        super(messager);
    }

    @Override
    protected boolean processLine(CompilerMessager messager, String line) {
        if (line == null) {
            return false;
        }
        if (line.startsWith(DelphiBackendCompiler.DELPHI_STARTER_RESPONSE)) {
            PascalCompilerMessager.createMessage(CompilerMessageCategory.ERROR, line, null, messager);
            return true;
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
                    PascalCompilerMessager.createMessage(CompilerMessageCategory.INFO, line, matcher, messager);
                } else {
                    PascalCompilerMessager.createMessage(CompilerMessageCategory.INFO, line, null, messager);
                }
            }
        }
        return true;
    }

}
