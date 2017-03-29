package com.siberika.idea.pascal.debugger;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.util.Key;

/**
 * Author: George Bakhtadze
 * Date: 28/03/2017
 */
public class GdbProcessAdapter implements ProcessListener {
    @Override
    public void startNotified(ProcessEvent event) {
        System.out.println("===*** STR" + event.getText());
    }

    @Override
    public void processTerminated(ProcessEvent event) {
        System.out.println("===*** TRM" + event.getText());
    }

    @Override
    public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
        System.out.println("===*** WTM" + event.getText());
    }

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
        System.out.println("===*** TXT" + event.getText());
    }
}
