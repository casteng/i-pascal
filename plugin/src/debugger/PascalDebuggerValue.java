package com.siberika.idea.pascal.debugger;

import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XNavigatable;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XErrorValuePresentation;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.debugger.gdb.GdbVariableObject;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.util.DocUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
        node.setPresentation(icon, new XValuePresentation() {

            @NotNull
            @Override
            public String getSeparator() {
                return ": ";
            }

            @Override
            public void renderValue(@NotNull XValueTextRenderer renderer) {
                if (variableObject.getType() != null) {
                    renderer.renderComment(variableObject.getType());
                    renderer.renderSpecialSymbol(" = ");
                }

                if (variableObject.getAdditional() != null) {
                    renderer.renderComment("(" + variableObject.getAdditional() + ") ");
                }
                String value = variableObject.getPresentation();
                if ((value != null) && (value.startsWith("'") || value.startsWith("#"))) {
                    renderer.renderValue(value, DefaultLanguageHighlighterColors.STRING);
                } else {
                    renderer.renderValue(value != null ? value : "??");
                }
            }

        }, variableObject.getChildrenCount() != null && variableObject.getChildrenCount() > 0);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (variableObject.getFrame().getProcess().backend.getData().getBoolean(PascalSdkData.Keys.DEBUGGER_RETRIEVE_CHILDS)) {
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
        if (doc != null) {
            int lineNum = sp.getLine();
            while (lineNum >= variableObject.getFrame().getBlockInfo().getStartLine()) {
                String line = DocUtil.getWholeLine(doc, lineNum);
                Pattern pattern = Pattern.compile("(?i)\\b" + variableObject.getName() + "\\b");
                if (pattern.matcher(line).find()) {
                    navigatable.setSourcePosition(XDebuggerUtil.getInstance().createPosition(file.getVirtualFile(), lineNum));
                    return;
                }
                lineNum--;
            }
        }
    }
}
