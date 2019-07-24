package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
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
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueContainerNode;
import com.siberika.idea.pascal.debugger.CommandSender;
import com.siberika.idea.pascal.debugger.PascalDebuggerValue;
import com.siberika.idea.pascal.debugger.PascalXDebugProcess;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Author: George Bakhtadze
 * Date: 01/04/2017
 */
public class GdbStackFrame extends XStackFrame {

    private static final Logger LOG = Logger.getInstance(GdbStackFrame.class);

    private static final String VAR_PREFIX_LOCAL = "l%";
    private static final String VAR_PREFIX_WATCHES = "w%";
    private static final String OPEN_ARRAY_HIGH_BOUND_VAR_PREFIX = "high";

    private final PascalXDebugProcess process;
    private final GdbExecutionStack executionStack;
    private final GdbMiResults frame;
    private final int level;
    private final Map<String, Collection<PasField>> fieldsMap = new ConcurrentHashMap<>();
    private XSourcePosition sourcePosition;
    private Map<String, GdbVariableObject> variableObjectMap;
    private XCompositeNode lastQueriedVariablesCompositeNode;
    private XCompositeNode lastParentNode;
    private final AtomicLong refreshCounter = new AtomicLong();

    public GdbStackFrame(GdbExecutionStack executionStack, GdbMiResults frame) {
        this.process = executionStack.getProcess();
        this.executionStack = executionStack;
        this.frame = frame;
        level = (frame != null) && (frame.getValue("level") != null) ? StrUtil.strToIntDef(frame.getString("level"), 0) : 0;
        if (process.options.needPosition()) {
            sourcePosition = getSourcePosition();
        }
        variableObjectMap = new LinkedHashMap<>();
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

    private PasField resolveIdentifierName(final String name, final Set<PasField.FieldType> types) {
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

    @Nullable
    @Override
    public Object getEqualityObject() {
        return getClass();
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        lastQueriedVariablesCompositeNode = node;
        process.sendCommand("-stack-select-frame " + level);
        process.sendCommand(String.format("-stack-list-variables --thread %s --frame %d --no-values", executionStack.getThreadId(), level), new CommandSender.FinishCallback() {
            @Override
            public void call(GdbMiLine res) {
                if (res.getResults().getValue("variables") != null) {
                    handleVariablesResponse(res.getResults().getList("variables"), new RefreshVariablesCallback(GdbStackFrame.this, refreshCounter.incrementAndGet()));
                } else {
                    LOG.info(String.format("DBG Error: Invalid debugger response for variables: %s", res.toString()));
                }
            }
        });
    }

    // handling of -stack-list-variables command
    private void handleVariablesResponse(List<Object> variables, CommandSender.FinishCallback callback) {
        clearVars();
        final int size = variables.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                Object o = variables.get(i);
                if (o instanceof GdbMiResults) {
                    GdbMiResults res = (GdbMiResults) o;
                    if (i < size - 1) {
                        createOrUpdateVar(res, true, null);
                    } else {                   // callback should be called for last var
                        createOrUpdateVar(res, true, callback);
                    }
                } else {
                    LOG.error(String.format("DBG Error: Invalid variables list entry: %s", o));
                }
            }
        } else {                  // No variables in frame. Callback should be called anyway.
            callback.call(new GdbMiLine(null, GdbMiLine.Type.RESULT_RECORD, "vars"));
        }
    }

    void evaluate(String expression, XDebuggerEvaluator.XEvaluationCallback callback) {
        String key = getVarKey(expression, false, VAR_PREFIX_WATCHES);
        GdbVariableObject var = variableObjectMap.get(key);
        if (null == var) {
            variableObjectMap.put(key, new GdbVariableObject(key, expression, expression, callback));
            process.sendCommand(String.format("-var-create %4$s%s%4$s %s \"%s\"", key, process.getVarFrame(), expression.toUpperCase(), process.getVarNameQuoteChar()));
        } else {
            var.setCallback(callback);
            updateVariableObjectUI(var);
//            process.sendCommand(String.format("-var-set-update-range %2$s%s%2$s 0 100", key, VAR_NAME_QUOTE_CHAR));
            process.sendCommand(String.format("-var-update --all-values %2$s%s%2$s", key, process.getVarNameQuoteChar()));
        }
    }

    public int getLevel() {
        return level;
    }

    private void refreshVariablesUI() {
        refreshVarTree(lastQueriedVariablesCompositeNode, new ArrayList<>(variableObjectMap.values()));
    }

    private void refreshVarTree(XCompositeNode node, Collection<GdbVariableObject> variableObjects) {
        if (null == node || node.isObsolete()) {
            return;
        }

        if (node instanceof XValueContainerNode) {
            try {
                if (variableObjects.isEmpty()) {
                    node.addChildren(XValueChildrenList.EMPTY, true);
                    return;
                }
                XValueChildrenList childrenList = new XValueChildrenList(variableObjects.size());
                for (GdbVariableObject var : variableObjects) {
                    if (var.isVisible()) {
                        childrenList.add(var.getName().substring(var.getName().lastIndexOf('.') + 1),
                                new PascalDebuggerValue(process, var.getKey(), var.getType(), var.getValue(), var.getChildrenCount(), var.getFieldType()));
                    }
                }
                node.addChildren(childrenList, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateVariableObjectUI(@NotNull GdbVariableObject var) {
        var.getCallback().evaluated(new PascalDebuggerValue(process, var.getKey(), var.getType(), var.getValue(), var.getChildrenCount()));
    }

    public void createOrUpdateVar(GdbMiResults res, boolean valueNeeded, CommandSender.FinishCallback callback) {
        String varName = res.getString("name");
        String varKey;
        if (varName.startsWith(VAR_PREFIX_LOCAL) || varName.startsWith(VAR_PREFIX_WATCHES)) {
            varKey = varName;
            varName = varName.substring(2);
        } else {
            varKey = getVarKey(varName, false, VAR_PREFIX_LOCAL);
        }
        GdbVariableObject var = variableObjectMap.get(varKey);
        if (var != null) {
            handleOpenArray(var, res);
            handleDynamicArray(var, res);
            var.updateFromResult(res);
            if (var.getCallback() != null) {
                updateVariableObjectUI(var);
            }
            if (valueNeeded) {
//                process.sendCommand(String.format("-var-set-update-range %2$s%s%2$s 0 100", varKey, VAR_NAME_QUOTE_CHAR));
                process.sendCommand(String.format("-var-update --all-values %2$s%s%2$s", varKey, process.getVarNameQuoteChar()), callback);
            }
        } else {
            String varNameResolved = varName;
            PasField.FieldType fieldType = PasField.FieldType.VARIABLE;
            boolean hidden = isHidden(varName, res);
            if (!hidden) {
                PasField field = resolveIdentifierName(varName, PasField.TYPES_LOCAL);
                if (field != null) {
                    varNameResolved = formatVariableName(field);
                    fieldType = field.fieldType;
                }
            }
            var = new GdbVariableObject(varKey, varNameResolved, varName, null, res);
            var.setFieldType(fieldType);
            var.setVisible(!hidden);
            variableObjectMap.put(varKey, var);
            if (valueNeeded) {
                process.sendCommand(String.format("-var-create %4$s%s%4$s %s \"%s\"", varKey, process.getVarFrame(), varName, process.getVarNameQuoteChar()), callback);
            }
        }
    }

    private boolean isHidden(String varName, GdbMiResults res) {
        return varName.startsWith(OPEN_ARRAY_HIGH_BOUND_VAR_PREFIX) || "result".equals(varName);
    }

    public void removeVar(String varKey) {
        variableObjectMap.remove(varKey);
    }

    private void clearVars() {
        variableObjectMap.clear();
    }

    private void handleOpenArray(GdbVariableObject highBoundVar, GdbMiResults res) {
        if (highBoundVar.getName().startsWith(OPEN_ARRAY_HIGH_BOUND_VAR_PREFIX)) {
            Integer value = res.getInteger("value");
            if (value != null) {
                final String openArrayName = highBoundVar.getName().substring(4);
                GdbVariableObject openArrayVar = variableObjectMap.get(getVarKey(openArrayName, false, VAR_PREFIX_LOCAL));
                if (openArrayVar != null) {
                    openArrayVar.setLength(value + 1);
                    process.sendCommand("type summary add -s " + openArrayVar.getKey() + "\"\\$${var[0-" + value + "]}\" -n " + openArrayVar.getKey());
                    process.sendCommand("fr v " + openArrayName + " --summary " + openArrayVar.getKey(), new RefreshVariablesCallback(GdbStackFrame.this, refreshCounter.incrementAndGet()));
                } else {
                    LOG.info(String.format("DBG Error: no array variable found for bound param %s", highBoundVar.getName()));
                }
            }
        }
    }

    private void handleDynamicArray(GdbVariableObject var, GdbMiResults res) {
        String type = res.getString("type");
        if (isDynamicArray(type)) {
            String addressStr = res.getString("value");
            if (addressStr != null) {
                Long address = Long.decode(addressStr);
                if (address == 0) {
                    return;
                }
                int size = addressStr.length() > 8 ? 8 : 4;
                refreshCounter.incrementAndGet();           // Increment to prevent refreshing UI by issued earlier -var-create
                process.sendCommand(String.format("-data-read-memory-bytes %s-%d %d", addressStr, size * 2, size * 2), new CommandSender.FinishCallback() {
                    @Override
                    public void call(GdbMiLine res) {
                        List<Object> memory = res.getResults().getValue("memory") != null ? res.getResults().getList("memory") : null;
                        GdbMiResults tuple = ((memory != null) && (memory.size() > 0)) ? (GdbMiResults) memory.get(0) : null;
                        String content = tuple != null ? tuple.getString("contents") : null;
                        if ((content != null) && (content.length() == (size * 2 * 2))) {
                            long refCount = parseHex(content.substring(0, size * 2));
                            long length = parseHex(content.substring(size * 2));
                            process.sendCommand(String.format("type summary add -s %s\"\\$(%d@%d)${var[0-%d]}\" -n %s", var.getKey(), length + 1, refCount, length, var.getKey()));
                            process.sendCommand(String.format("fr v %s[0] --summary %s", var.getExpression(), var.getKey()), new RefreshVariablesCallback(GdbStackFrame.this, refreshCounter.incrementAndGet()));
                        } else {
                            LOG.info(String.format("DBG Error: Invalid debugger response for memory: %s", res.toString()));
                        }
                    }
                });
            }
        }
    }

    private long parseHex(String s) {
        int len = s.length();
        long data = 0;
        for (int i = 0; i < len; i += 2) {
            data = data * 256 + (byte) ((Character.digit(s.charAt(len-2-i), 16) << 4)
                    + Character.digit(s.charAt(len-1-i), 16));
        }
        return data;
    }

    private boolean isDynamicArray(String type) {
        return (type != null) && type.contains("(*)[]");
    }

    private String formatVariableName(@NotNull PasField field) {
        return field.name + (field.fieldType == PasField.FieldType.ROUTINE ? "()" : "");
    }

    private String getVarKey(String varName, boolean children, String prefix) {
        return (children ? "" : prefix) + varName.replace(' ', '_');
    }

    public static class RefreshVariablesCallback implements CommandSender.FinishCallback {
        private final GdbStackFrame stackFrame;
        private final long refreshValue;

        RefreshVariablesCallback(GdbStackFrame stackFrame, long refreshValue) {
            this.stackFrame = stackFrame;
            this.refreshValue = refreshValue;
        }

        @Override
        public void call(GdbMiLine res) {
            stackFrame.process.handleResponse(res);
            if (stackFrame.refreshCounter.get() == refreshValue) {
                stackFrame.refreshVariablesUI();
            }
        }
    }

}
