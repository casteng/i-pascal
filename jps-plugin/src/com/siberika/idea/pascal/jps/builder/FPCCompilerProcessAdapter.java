package com.siberika.idea.pascal.jps.builder;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.util.Key;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import com.siberika.idea.pascal.jps.compiler.FPCOutputParser;

/**
 * Author: George Bakhtadze
 * Date: 12/05/2014
 */
public class FPCCompilerProcessAdapter extends ProcessAdapter {
    private final CompilerMessager messager;

    public FPCCompilerProcessAdapter(CompilerMessager messager) {
        this.messager = messager;
    }

    public CompilerMessager getMessager() {
        return messager;
    }

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
        FPCOutputParser.processLine(messager, event.getText());
    }
}
