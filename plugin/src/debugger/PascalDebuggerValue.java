package com.siberika.idea.pascal.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class PascalDebuggerValue extends XValue {

    private final String name;
    private final String type;
    private final String value;
    private final Integer numChilds;

    public PascalDebuggerValue(String name, String type, String value, Integer numChilds) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.numChilds = numChilds;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        node.setPresentation(AllIcons.Nodes.Variable, type != null ? type : "??", value != null ? value : "??", numChilds != null && numChilds > 0);
    }
}
