package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.codeInspection.SmartHashMap;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.siberika.idea.pascal.debugger.DebugThread;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 31/03/2017
 */
public class GdbSuspendContext extends XSuspendContext {
    private final PascalXDebugProcess process;
    private final XExecutionStack stack;
    private final Map<Integer, XExecutionStack> stackMap = new SmartHashMap<>();

    public GdbSuspendContext(PascalXDebugProcess process, List<DebugThread> threadsLine, GdbMiLine stopContext) {
        this.process = process;
        stackMap.clear();
        for (DebugThread thread : threadsLine) {
            stackMap.put(thread.getId(), new GdbExecutionStack(this, thread));
        }
        this.stack = retrieveCurrentStack(stopContext);
    }

    @Nullable
    @Override
    public XExecutionStack getActiveExecutionStack() {
        return stack;
    }

    @NotNull
    @Override
    public XExecutionStack[] getExecutionStacks() {
        return stackMap.values().toArray(new XExecutionStack[0]);
    }

    public PascalXDebugProcess getProcess() {
        return process;
    }

    private XExecutionStack retrieveCurrentStack(GdbMiLine stopContext) {
        Integer stoppedThreadId = stopContext.getResults().getInteger("thread-id");
        if (stoppedThreadId != null) {
            for (Map.Entry<Integer, XExecutionStack> entry : stackMap.entrySet()) {
                if (entry.getKey().equals(stoppedThreadId)) {
                    return entry.getValue();
                }
            }
        }
        return new GdbExecutionStack(this, stopContext);
    }

}
