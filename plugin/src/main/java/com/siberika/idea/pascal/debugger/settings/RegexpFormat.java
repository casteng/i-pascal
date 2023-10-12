package com.siberika.idea.pascal.debugger.settings;

import org.jetbrains.annotations.NotNull;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.regex.Pattern;

public class RegexpFormat extends Format {
    private final Pattern pattern;

    public RegexpFormat(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public StringBuffer format(Object obj, @NotNull StringBuffer toAppendTo, @NotNull FieldPosition pos) {
        toAppendTo.append(obj);
        return toAppendTo;
    }

    @Override
    public Object parseObject(String source, @NotNull ParsePosition pos) {
        if (pattern.matcher(source).matches()) {
            pos.setIndex(source.length());
            return source;
        } else {
            return null;
        }
    }
}
