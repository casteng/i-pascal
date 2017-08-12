package com.siberika.idea.pascal.debugger;

import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class PascalDebuggerValue extends XValue {

    private final PascalXDebugProcess debugProcess;
    private final String name;
    private final String type;
    private final String value;
    private final Integer childrenCount;
    private final PasField.FieldType fieldType;

    public PascalDebuggerValue(PascalXDebugProcess debugProcess, String name, String type, String value, Integer childrenCount, PasField.FieldType fieldType) {
        this.debugProcess = debugProcess;
        this.name = name;
        this.type = type;
        this.value = value;
        this.childrenCount = childrenCount;
        this.fieldType = fieldType;
    }

    public PascalDebuggerValue(PascalXDebugProcess debugProcess, String name, String type, String value, Integer childrenCount) {
        this(debugProcess, name, type, value, childrenCount, PasField.FieldType.VARIABLE);
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
        if (debugProcess.getData().getBoolean(PascalSdkData.Keys.DEBUGGER_RETRIEVE_CHILDS)) {
            debugProcess.computeValueChildren(name, node);
        } else {
            node.setErrorMessage(PascalBundle.message("debug.error.subfields.disabled"));
        }
    }

}
