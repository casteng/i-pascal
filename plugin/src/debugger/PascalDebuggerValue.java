package com.siberika.idea.pascal.debugger;

import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XNavigatable;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XErrorValuePresentation;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.debugger.gdb.GdbVariableObject;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.util.DocUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class PascalDebuggerValue extends XValue {

    private GdbVariableObject variableObject;

    public PascalDebuggerValue(GdbVariableObject variableObject) {
        this.variableObject = variableObject;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        if (variableObject.getError() != null) {
            node.setPresentation(null, new XErrorValuePresentation(variableObject.getError()), false);
            return;
        }
        Icon icon = PascalIcons.VARIABLE;
        switch (variableObject.getFieldType()) {
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
        node.setPresentation(icon, (variableObject.getType() != null ? variableObject.getType() : "??") + "| " + variableObject.getAdditional(),
                variableObject.getPresentation() != null ? variableObject.getPresentation() : "??",
                variableObject.getChildrenCount() != null && variableObject.getChildrenCount() > 0);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (variableObject.getFrame().getProcess().getData().getBoolean(PascalSdkData.Keys.DEBUGGER_RETRIEVE_CHILDS)) {
            variableObject.getFrame().getProcess().getVariableManager().computeValueChildren(variableObject.getKey(), node);
        } else {
            node.setErrorMessage(PascalBundle.message("debug.error.subfields.disabled"));
        }
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable navigatable) {
        XSourcePosition sp = variableObject.getFrame().getSourcePosition();
        PsiFile file = sp != null ? DebuggerUtilsEx.getPsiFile(sp, variableObject.getFrame().getProcess().getProject()) : null;
        Document doc = file != null ? PsiDocumentManager.getInstance(variableObject.getFrame().getProcess().getProject()).getDocument(file) : null;
        String line = doc != null ? DocUtil.getWholeLineAt(doc, sp.getOffset()) : null;
        Pattern pattern = Pattern.compile("(?i)\\b" + variableObject.getName() + "\\b");
        if (line != null) {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                navigatable.setSourcePosition(variableObject.getFrame().getSourcePosition());
            }
        }
    }
}
