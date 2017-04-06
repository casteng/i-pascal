package com.siberika.idea.pascal.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.siberika.idea.pascal.debugger.gdb.PascalXDebugProcess;
import org.jetbrains.annotations.NotNull;

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

    public PascalDebuggerValue(PascalXDebugProcess debugProcess, String name, String type, String value, Integer childrenCount) {
        this.debugProcess = debugProcess;
        this.name = name;
        this.type = type;
        this.value = value;
        this.childrenCount = childrenCount;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        node.setPresentation(AllIcons.Nodes.Variable, type != null ? type : "??", value != null ? value : "??", childrenCount != null && childrenCount > 0);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        debugProcess.computeValueChildren(name, node);
    }

}
