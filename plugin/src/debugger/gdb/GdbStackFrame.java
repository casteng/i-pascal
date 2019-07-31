package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueContainerNode;
import com.siberika.idea.pascal.debugger.PascalDebuggerValue;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.VariableManager;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author: George Bakhtadze
 * Date: 01/04/2017
 */
public class GdbStackFrame extends XStackFrame {

    private static final Logger LOG = Logger.getInstance(GdbStackFrame.class);

    private final PascalXDebugProcess process;
    private final GdbExecutionStack executionStack;
    private final GdbMiResults frame;
    private final int level;
    private final VariableManager variableManager;
    private XSourcePosition sourcePosition;
    private AtomicReference<XCompositeNode> nodeRef = new AtomicReference<>();
    private Collection<GdbVariableObject> variableObjects;

    public GdbStackFrame(GdbExecutionStack executionStack, GdbMiResults frame) {
        this.process = executionStack.getProcess();
        this.variableManager = process.getVariableManager();
        this.executionStack = executionStack;
        this.frame = frame;
        level = (frame != null) && (frame.getValue("level") != null) ? StrUtil.strToIntDef(frame.getString("level"), 0) : 0;
        if (process.options.needPosition()) {
            sourcePosition = getSourcePosition();
        }
    }

    @Override
    public XSourcePosition getSourcePosition() {
        if (sourcePosition != null) {
            return sourcePosition;
        }
        if (null == frame) {
            return null;
        }
        Integer line = frame.getInteger("line");
        String filename = frame.getString("fullname");
        if ((null == filename) || (null == line)) {
            return null;
        }

        String path = filename.replace(File.separatorChar, '/');
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
        if (null == virtualFile) {
            return null;
        }

        if (null == sourcePosition) {
            sourcePosition = XDebuggerUtil.getInstance().createPosition(virtualFile, line - 1);
        }

        return sourcePosition;
    }

    @Override
    public void customizePresentation(@NotNull ColoredTextContainer component) {
        if (null == frame) {
            return;
        }
        String filename = frame.getString("fullname");
        filename = filename != null ? FileUtil.getFilename(filename) : "-";
        String line = frame.getString("line");
        component.append(formatRoutine(frame), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        component.append(String.format(" (%s:%s)", filename, line != null ? line : "-"), SimpleTextAttributes.GRAYED_ATTRIBUTES);
    }

    private String formatRoutine(GdbMiResults frame) {
        String name = frame.getString("func");
        if (process.options.resolveNames() && (sourcePosition != null)) {
            PsiElement el = XDebuggerUtil.getInstance().findContextElement(sourcePosition.getFile(), sourcePosition.getOffset(), process.getSession().getProject(), false);
            PasEntityScope scope = el != null ? PsiUtil.getNearestAffectingScope(el) : null;
            if (scope instanceof PascalRoutine) {
                name = scope.getName();
            } else if (scope instanceof PascalModule) {
                name = String.format("%s(%s)", name, scope.getName());
            }
        }
        if (StringUtils.isEmpty(name) || "??".equals(name)) {
            String addr = frame.getString("addr");
            name = String.format("?? (%s)", addr != null ? addr : "-");
        }
        return name + "()";
    }

    @Nullable
    @Override
    public XDebuggerEvaluator getEvaluator() {
        return new GdbEvaluator(this, variableManager);
    }

    @Nullable
    @Override
    public Object getEqualityObject() {
        return getClass();
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        this.nodeRef.set(node);
        refreshVarTree(node, variableObjects);
    }

    public PascalXDebugProcess getProcess() {
        return process;
    }

    public int getLevel() {
        return level;
    }

    public void queryVariables() {
        variableManager.queryVariables(level, executionStack, this);
    }

    public void refreshVarTree(Collection<GdbVariableObject> variableObjects) {
        this.variableObjects = variableObjects;
        if (refreshVarTree(nodeRef.get(), variableObjects)) {
//            this.variableObjects = null;
        }
    }

    public boolean refreshVarTree(XCompositeNode node, Collection<GdbVariableObject> variableObjects) {
        if (null == node || node.isObsolete()) {
            LOG.info("DBG Warn: variables node is not ready");
            return false;
        }

        if (node instanceof XValueContainerNode) {
            try {
                if ((variableObjects == null) || variableObjects.isEmpty()) {
                    node.addChildren(XValueChildrenList.EMPTY, true);
                    return false;
                }
                XValueChildrenList childrenList = new XValueChildrenList(variableObjects.size());
                for (GdbVariableObject var : variableObjects) {
                    if (var.isVisible() && !var.isWatched()) {
                        childrenList.add(var.getExpression().substring(var.getExpression().lastIndexOf('.') + 1),
                                new PascalDebuggerValue(this, var.getKey(), var.getType(), var.getPresentation(), var.getChildrenCount(), var.getFieldType()));
                    }
                }
                node.addChildren(childrenList, true);
            } catch (Exception e) {
                LOG.error("DBG Error: exception while adding children", e);
            }
        }
        return true;
    }

}
