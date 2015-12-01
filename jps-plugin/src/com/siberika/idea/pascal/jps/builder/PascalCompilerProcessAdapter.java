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
    private StringBuilder sb = new StringBuilder();

    public PascalCompilerProcessAdapter(CompilerMessager messager) {
        this.messager = messager;
    }

    abstract protected boolean processLine(CompilerMessager messager, String text);

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
        String str = event.getText();
        sb.append(str);
        if (str.endsWith("\n")) {
            doProcessLine();
        }
    }

    @Override
    public void processTerminated(ProcessEvent event) {
        super.processTerminated(event);
        if (sb.length() > 0) {
            doProcessLine();
        }
    }

    private void doProcessLine() {
        processLine(messager, sb.toString());
        sb = new StringBuilder();
    }

}
