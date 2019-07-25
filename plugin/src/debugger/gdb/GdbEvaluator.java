package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class GdbEvaluator extends XDebuggerEvaluator {
    private final GdbStackFrame gdbStackFrame;

    GdbEvaluator(GdbStackFrame gdbStackFrame) {
        this.gdbStackFrame = gdbStackFrame;
    }

    @Override
    public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback, @Nullable XSourcePosition expressionPosition) {
        gdbStackFrame.evaluate(expression, callback);
    }
}
