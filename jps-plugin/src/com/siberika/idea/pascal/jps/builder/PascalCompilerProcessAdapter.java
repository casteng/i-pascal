package com.siberika.idea.pascal.jps.builder;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.util.Key;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;

/**
 * Author: George Bakhtadze
 * Date: 12/05/2014
 */
public abstract class PascalCompilerProcessAdapter extends ProcessAdapter {
    private final CompilerMessager messager;

    public PascalCompilerProcessAdapter(CompilerMessager messager) {
        this.messager = messager;
    }

    abstract protected boolean processLine(CompilerMessager messager, String text);

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
        processLine(messager, event.getText());
    }

}
