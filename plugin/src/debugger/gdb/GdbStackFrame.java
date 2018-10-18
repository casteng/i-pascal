package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Computable;
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
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.util.StrUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Author: George Bakhtadze
 * Date: 01/04/2017
 */
public class GdbStackFrame extends XStackFrame {
    private final PascalXDebugProcess process;
    private final GdbExecutionStack executionStack;
    private final GdbMiResults frame;
    private final int level;
    private final ConcurrentMap<String, Collection<PasField>> fieldsMap = new ConcurrentHashMap<String, Collection<PasField>>();
    private XSourcePosition sourcePosition;

    public GdbStackFrame(GdbExecutionStack executionStack, GdbMiResults frame) {
        this.process = executionStack.getProcess();
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
        component.setIcon(AllIcons.Debugger.StackFrame);
    }

    private String formatRoutine(GdbMiResults frame) {
        String name = frame.getString("func");
        PasField routine = resolveIdentifierName(name, PasField.TYPES_ROUTINE);
        name = routine != null ? routine.name : name;
        if (StringUtils.isEmpty(name) || "??".equals(name)) {
            String addr = frame.getString("addr");
            name = String.format("?? (%s)", addr != null ? addr : "-");
        }
        return name + "()";
    }

    public PasField resolveIdentifierName(final String name, final Set<PasField.FieldType> types) {
        if (!process.options.resolveNames() || (null == sourcePosition)) {
            return null;
        }
        if (DumbService.isDumb(process.getSession().getProject())) {
            return null;
        } else {
            return ApplicationManager.getApplication().runReadAction(new Computable<PasField>() {
                @Override
                public PasField compute() {
                    PsiElement el = XDebuggerUtil.getInstance().findContextElement(sourcePosition.getFile(), sourcePosition.getOffset(), process.getSession().getProject(), false);
                    if (el != null) {
                        Collection<PasField> fields = getFields(el, name);
                        String id = name.substring(name.lastIndexOf('.') + 1);
                        for (PasField field : fields) {
                            if (types.contains(field.fieldType) && id.equalsIgnoreCase(field.name)) {
                                return field;
                            }
                        }
                    }
                    return null;
                }
            });
        }
    }

    private Collection<PasField> getFields(@NotNull PsiElement el, String name) {
        Collection<PasField> fields = fieldsMap.get(name);
        if (fields != null) {
            return fields;
        }
        NamespaceRec namespace;
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            if (name.startsWith("this.")) {
                namespace = NamespaceRec.fromFQN(el, name.substring(5));
            } else {
                namespace = NamespaceRec.fromFQN(el, name);
            }
        } else {
            namespace = NamespaceRec.fromFQN(el, PasField.DUMMY_IDENTIFIER);
        }
        namespace.clearTarget();
        namespace.setIgnoreVisibility(true);
        fields = PasReferenceUtil.resolveExpr(namespace, new ResolveContext(PasField.TYPES_LOCAL, true), 0);
        fieldsMap.put(name, fields);
        return fields;
    }

    @Nullable
    @Override
    public XDebuggerEvaluator getEvaluator() {
        return new GdbEvaluator(this);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        process.setLastQueriedVariablesCompositeNode(node);
        process.sendCommand(String.format("-stack-list-variables --thread %s --frame %d --simple-values", executionStack.getThreadId(), level));
    }

    public GdbExecutionStack getExecutionStack() {
        return executionStack;
    }

    public int getLevel() {
        return level;
    }
}
