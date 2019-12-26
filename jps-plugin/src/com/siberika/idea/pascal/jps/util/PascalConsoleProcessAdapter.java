package com.siberika.idea.pascal.jps.util;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public abstract class PascalConsoleProcessAdapter extends ProcessAdapter {
    private StringBuffer sb = new StringBuffer();

    abstract public boolean onLine(String text);

    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        String str = event.getText();
        sb.append(str);
        if (str.endsWith("\r\n") || str.endsWith("\n")) {
            doProcessLine();
        }
    }

    @Override
    public void processTerminated(@NotNull ProcessEvent event) {
        super.processTerminated(event);
        if (sb.length() > 0) {
            doProcessLine();
        }
    }

    private void doProcessLine() {
        onLine(sb.toString());
        sb = new StringBuffer();
    }

}
