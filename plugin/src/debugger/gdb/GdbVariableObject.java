package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class GdbVariableObject {
    private final GdbStackFrame frame;
    private final String key;
    // name in debugger
    private final String name;
    // expression entered by user or resolved name
    private String expression;
    private String additional;
    private String error;

    private XDebuggerEvaluator.XEvaluationCallback callback;

    private String type;
    private String value;
    private String valueRefined;
    private Integer childrenCount;
    private long length;
    private PasField.FieldType fieldType = PasField.FieldType.VARIABLE;
    private boolean visible = true;

    private List<GdbVariableObject> children;

    public GdbVariableObject(GdbStackFrame frame, String key, String name, String expression, XDebuggerEvaluator.XEvaluationCallback callback) {
        this.frame = frame;
        this.key = key;
        this.name = name;
        this.expression = expression;
        this.callback = callback;
    }

    public GdbVariableObject(GdbStackFrame frame, String key, String name, String expression, XDebuggerEvaluator.XEvaluationCallback callback, GdbMiResults res) {
        this(frame, key, name, expression, callback);
        updateFromResult(res);
    }

    public GdbStackFrame getFrame() {
        return frame;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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
        childrenCount = res.getValue("numchild") != null ? res.getInteger("numchild") : 1;
    }

    public void update(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setValueRefined(String valueRefined) {
        this.valueRefined = valueRefined;
    }

    // May not be the same as getChildren().getSize()
    public Integer getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(Integer childrenCount) {
        this.childrenCount = childrenCount;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public PasField.FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(PasField.FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isWatched() {
        return this.callback != null;
    }

    public String getPresentation() {
        return valueRefined != null ? valueRefined : value;
    }

    public List<GdbVariableObject> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public GdbVariableObject findChild(String id) {
        if (children != null) {
            int lastDotIndex = id.indexOf('.');
            if (lastDotIndex > 0) {
                GdbVariableObject res = doFindChild(id.substring(0, lastDotIndex));
                if (res != null) {
                    return res.findChild(id.substring(lastDotIndex + 1));
                }
            } else {
                return doFindChild(id);
            }
        }
        return null;
    }

    private GdbVariableObject doFindChild(String id) {
        for (GdbVariableObject child : children) {
            if (id.equals(child.name)) {
                return child;
            }
        }
        return null;
    }
}
