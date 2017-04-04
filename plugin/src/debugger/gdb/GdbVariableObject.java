package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class GdbVariableObject {
    private final String expression;

    private XDebuggerEvaluator.XEvaluationCallback callback;

    private String type;
    private String value;
    private Integer childrenCount;

    public GdbVariableObject(String expression, XDebuggerEvaluator.XEvaluationCallback callback) {
        this.expression = expression;
        this.callback = callback;
    }

    public String getExpression() {
        return expression;
    }

    public XDebuggerEvaluator.XEvaluationCallback getCallback() {
        return callback;
    }

    public void setCallback(XDebuggerEvaluator.XEvaluationCallback callback) {
        this.callback = callback;
    }

    public void updateFromResult(GdbMiResults res) {
        type = res.getString("type");
        value = res.getString("value");
        childrenCount = res.getInteger("numchild");
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Integer getChildrenCount() {
        return childrenCount;
    }
}
