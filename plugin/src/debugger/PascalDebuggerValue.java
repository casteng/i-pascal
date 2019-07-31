package com.siberika.idea.pascal.debugger;

import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.debugger.gdb.GdbStackFrame;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class PascalDebuggerValue extends XValue {

    private final GdbStackFrame frame;
    private final String name;
    private final String type;
    private final String value;
    private final Integer childrenCount;
    private final PasField.FieldType fieldType;

    public PascalDebuggerValue(GdbStackFrame frame, String name, String type, String value, Integer childrenCount, PasField.FieldType fieldType) {
        this.frame = frame;
        this.name = name;
        this.type = type;
        this.value = value;
        this.childrenCount = childrenCount;
        this.fieldType = fieldType != null ? fieldType : PasField.FieldType.VARIABLE;
    }

    public PascalDebuggerValue(GdbStackFrame frame, String name, String type, String value, Integer childrenCount) {
        this(frame, name, type, value, childrenCount, PasField.FieldType.VARIABLE);
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        Icon icon = PascalIcons.VARIABLE;
        switch (fieldType) {
            case ROUTINE: {
                icon = PascalIcons.ROUTINE;
                break;
            }
            case CONSTANT: {
                icon = PascalIcons.CONSTANT;
                break;
            }
            case PROPERTY: {
                icon = PascalIcons.PROPERTY;
                break;
            }
            case PSEUDO_VARIABLE: {
                icon = PascalIcons.COMPILED;
                break;
            }
        }
        node.setPresentation(icon, type != null ? type : "??", value != null ? value : "??", childrenCount != null && childrenCount > 0);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (frame.getProcess().getData().getBoolean(PascalSdkData.Keys.DEBUGGER_RETRIEVE_CHILDS)) {
            frame.getProcess().getVariableManager().computeValueChildren(name, node);
        } else {
            node.setErrorMessage(PascalBundle.message("debug.error.subfields.disabled"));
        }
    }

}
