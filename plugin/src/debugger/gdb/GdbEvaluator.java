package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.siberika.idea.pascal.debugger.VariableManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class GdbEvaluator extends XDebuggerEvaluator {
    private final GdbStackFrame frame;
    private final VariableManager variableManager;

    GdbEvaluator(GdbStackFrame frame, VariableManager variableManager) {
        this.frame = frame;
        this.variableManager = variableManager;
    }

    @Override
    public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback, @Nullable XSourcePosition expressionPosition) {
        variableManager.evaluate(frame, expression, callback, expressionPosition);
    }
}
