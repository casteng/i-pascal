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
    private final String key;
    // name in debugger
    private final String name;
    // expression entered by user or resolved name
    private String expression;

    private XDebuggerEvaluator.XEvaluationCallback callback;

    private String type;
    private String value;
    private String valueRefined;
    private Integer childrenCount;
    private int length;
    private PasField.FieldType fieldType;
    private boolean visible = true;

    private List<GdbVariableObject> children;

    public GdbVariableObject(String key, String name, String expression, XDebuggerEvaluator.XEvaluationCallback callback) {
        this.key = key;
        this.name = name;
        this.expression = expression;
        this.callback = callback;
    }

    public GdbVariableObject(String key, String name, String expression, XDebuggerEvaluator.XEvaluationCallback callback, GdbMiResults res) {
        this(key, name, expression, callback);
        updateFromResult(res);
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

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
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
            for (GdbVariableObject child : children) {
                if (id.equals(child.name)) {
                    return child;
                }
            }
        }
        return null;
    }
}
