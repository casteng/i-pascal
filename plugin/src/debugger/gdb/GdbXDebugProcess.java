package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.debugger.PascalDebuggerValue;
import com.siberika.idea.pascal.debugger.PascalLineBreakpointHandler;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.run.PascalRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class GdbXDebugProcess extends XDebugProcess {

    private final XBreakpointHandler<?>[] MY_BREAKPOINT_HANDLERS = new XBreakpointHandler[] {new PascalLineBreakpointHandler(this)};
    private final ExecutionEnvironment environment;
    private final ExecutionResult executionResult;
    private ConsoleView myExecutionConsole;
    private XCompositeNode lastQueriedVariablesCompositeNode;
    private XCompositeNode lastParentNode;
    private Map<String, GdbVariableObject> variableObjectMap;

    private static final String VAR_PREFIX_LOCAL = "l%";
    private static final String VAR_PREFIX_WATCHES = "w%";

    public GdbXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        super(session);
        this.environment = environment;
        this.executionResult = executionResult;
        try {
            createGdbProcess(environment);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void createGdbProcess(ExecutionEnvironment env) throws ExecutionException {
        RunProfile profile = env.getRunProfile();
        if (profile instanceof PascalRunConfiguration) {
            variableObjectMap = new HashMap<String, GdbVariableObject>();
            sendCommand("-break-delete");
        }
    }

    @Nullable
    @Override
    protected ProcessHandler doGetProcessHandler() {
        return executionResult.getProcessHandler();
    }

    @NotNull
    @Override
    public ExecutionConsole createConsole() {
        myExecutionConsole = (ConsoleView) executionResult.getExecutionConsole();
        return myExecutionConsole;
    }

    public void printToConsole(String text, ConsoleViewContentType contentType) {
        if (myExecutionConsole != null) {
            myExecutionConsole.print(text, contentType);
        }
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
        getProcessHandler().addProcessListener(new GdbProcessAdapter(this));
        sendCommand("-gdb-set target-async on");
        sendCommand("-exec-run");
        getSession().setPauseActionSupported(true);
    }

    @Override
    public void startPausing() {
        sendCommand("-exec-interrupt");
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        sendCommand("-exec-continue --all");
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        sendCommand("-exec-next");
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        sendCommand("-exec-step");
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        sendCommand("-exec-finish");
    }

    @Override
    public void stop() {
        // finalize something
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        sendCommand(String.format("-exec-until %s:%d", position.getFile().getCanonicalPath(), position.getLine()));
    }

    public void sendCommand(String command) {
        try {
            OutputStream commandStream = getProcessHandler().getProcessInput();
            if (commandStream != null) {
                commandStream.write((command + "\n").getBytes("UTF-8"));
                commandStream.flush();
                printToConsole(">>>> " + command + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return new XDebuggerEditorsProvider() {
            @NotNull
            @Override
            public FileType getFileType() {
                return PascalFileType.INSTANCE;
            }

            @NotNull
            @Override
            public Document createDocument(@NotNull Project project,
                                           @NotNull String text,
                                           @Nullable XSourcePosition sourcePosition,
                                           @NotNull EvaluationMode mode) {
//                PsiFile file = PsiManager.getInstance(project).findFile(sourcePosition.getFile());
//                return PsiDocumentManager.getInstance(project).getDocument(file);
                LightVirtualFile file = new LightVirtualFile("_debug.pas", text);
                return FileDocumentManager.getInstance().getDocument(file);
            }
        };
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return MY_BREAKPOINT_HANDLERS;
    }

    public PascalLineBreakpointHandler getBreakpointHandler() {
        return (PascalLineBreakpointHandler) MY_BREAKPOINT_HANDLERS[0];
    }

    public void setLastQueriedVariablesCompositeNode(XCompositeNode lastQueriedVariablesCompositeNode) {
        this.lastQueriedVariablesCompositeNode = lastQueriedVariablesCompositeNode;
    }

    public void evaluate(String expression, XDebuggerEvaluator.XEvaluationCallback callback) {
        String key = VAR_PREFIX_WATCHES + expression;
        GdbVariableObject var = variableObjectMap.get(key);
        if (null == var) {
            variableObjectMap.put(key, new GdbVariableObject(key, expression, callback));
            sendCommand(String.format("-var-create \"%s\" @ \"%s\"", key, expression));
        } else {
            var.setCallback(callback);
            updateVariableObjectUI(var);
            sendCommand(String.format("-var-update --all-values \"%s\"", key));
        }
    }

    public void handleVarResult(GdbMiResults res) {
        String key = res.getString("name");
        GdbVariableObject var = variableObjectMap.get(key);
        if (var != null) {
            var.updateFromResult(res);
            if (var.getCallback() != null) {
                updateVariableObjectUI(var);
            }
        }
    }

    private void updateVariableObjectUI(@NotNull GdbVariableObject var) {
        var.getCallback().evaluated(new PascalDebuggerValue(this, var.getKey(), var.getType(), var.getValue(), var.getChildrenCount()));
    }

    public void handleVarUpdate(GdbMiResults results) {
        List<Object> changes = results.getList("changelist");
        for (Object o : changes) {
            GdbMiResults change = (GdbMiResults) o;
            handleVarResult(change);
        }
    }

    synchronized public void computeValueChildren(String name, XCompositeNode node) {
        lastParentNode = node;
        sendCommand("-var-list-children --all-values " + name);
    }

    synchronized void handleVariablesResponse(List<Object> variables) {
        handleVariables(lastQueriedVariablesCompositeNode, variables, false);
    }

    synchronized void handleChildrenResult(List<Object> variables) {
        handleVariables(lastParentNode, variables, true);
        lastParentNode = null;
    }

    private void handleVariables(XCompositeNode node, List<Object> variables, boolean children) {
        if (null == node) {
            return;
        }
        if (variables.isEmpty()) {
            node.addChildren(XValueChildrenList.EMPTY, true);
        } else {
            XValueChildrenList childrenList = new XValueChildrenList(variables.size());
            for (Object o : variables) {
                if (o instanceof GdbMiResults) {
                    GdbMiResults res = (GdbMiResults) o;
                    if (children) {
                        res = res.getTuple("child");
                    }
                    String varName = res.getString("name");
                    String varKey = (children ? "" : VAR_PREFIX_LOCAL) + varName;
                    GdbVariableObject var = variableObjectMap.get(varKey);
                    if (null != var) {
                        var.updateFromResult(res);
                        if (!children) {
                            sendCommand(String.format("-var-update --all-values \"%s\"", varKey));
                        }
                    } else {
                        var = new GdbVariableObject(varKey, varName, null, res);
                        variableObjectMap.put(varKey, var);
                        if (!children) {
                            sendCommand(String.format("-var-create \"%s\" @ \"%s\"", varKey, varName));
                        }
                    }

                    childrenList.add(varName.substring(varName.lastIndexOf('.')+1),
                            new PascalDebuggerValue(this, var.getKey(), var.getType(), var.getValue(), var.getChildrenCount()));
                } else {
                    node.setErrorMessage("Invalid variables list entry");
                    return;
                }
            }
            node.addChildren(childrenList, true);
        }
    }

}
