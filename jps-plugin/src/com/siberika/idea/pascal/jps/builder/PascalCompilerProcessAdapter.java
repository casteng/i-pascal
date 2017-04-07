package com.siberika.idea.pascal.jps.builder;

import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import com.siberika.idea.pascal.jps.util.PascalConsoleProcessAdapter;

/**
 * Author: George Bakhtadze
 * Date: 12/05/2014
 */
public abstract class PascalCompilerProcessAdapter extends PascalConsoleProcessAdapter {
    private final CompilerMessager messager;

    public PascalCompilerProcessAdapter(CompilerMessager messager) {
        this.messager = messager;
    }

    abstract protected boolean processLine(CompilerMessager messager, String text);

    @Override
    protected boolean onLine(String text) {
        return processLine(messager, text);
    }
}
