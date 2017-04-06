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
public class PascalXDebugProcess extends XDebugProcess {

    private final XBreakpointHandler<?>[] MY_BREAKPOINT_HANDLERS = new XBreakpointHandler[] {new PascalLineBreakpointHandler(this)};
    private final ExecutionEnvironment environment;
    private final ExecutionResult executionResult;
    private ConsoleView myExecutionConsole;
    private XCompositeNode lastQueriedVariablesCompositeNode;
    private Map<String, GdbVariableObject> variableObjectMap;

    public PascalXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
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

    public XCompositeNode getLastQueriedVariablesCompositeNode() {
        return lastQueriedVariablesCompositeNode;
    }

    public void setLastQueriedVariablesCompositeNode(XCompositeNode lastQueriedVariablesCompositeNode) {
        this.lastQueriedVariablesCompositeNode = lastQueriedVariablesCompositeNode;
    }

    public void evaluate(String expression, XDebuggerEvaluator.XEvaluationCallback callback) {
        GdbVariableObject var = variableObjectMap.get(expression);
        if (null == var) {
            variableObjectMap.put(expression, new GdbVariableObject(expression, callback));
            sendCommand(String.format("-var-create \"%s\" @ \"%s\"", expression, expression));
        } else {
            var.setCallback(callback);
            updateVariableObjectUI(var);
            sendCommand(String.format("-var-update --all-values \"%s\"", expression));
        }
    }

    public void handleVarResult(GdbMiResults res) {
        String key = res.getString("name");
        GdbVariableObject var = variableObjectMap.get(key);
        if (var != null) {
            var.updateFromResult(res);
            updateVariableObjectUI(var);
        }
    }

    private void updateVariableObjectUI(@NotNull GdbVariableObject var) {
        var.getCallback().evaluated(new PascalDebuggerValue(this, var.getExpression(), var.getType(), var.getValue(), var.getChildrenCount()));
    }

    public void handleVarUpdate(GdbMiResults results) {
        List<Object> changes = results.getList("changelist");
        for (Object o : changes) {
            GdbMiResults change = (GdbMiResults) o;
            handleVarResult(change);
        }
    }

    private XCompositeNode lastParentNode;
    synchronized public void computeValueChildren(String name, XCompositeNode node) {
        lastParentNode = node;
        sendCommand("-var-list-children --all-values " + name);
    }

    synchronized public void handleChildrenResult(GdbMiResults results) {
        if (null == lastParentNode) {
            return;
        }
        XValueChildrenList childrenList = new XValueChildrenList();
        List<Object> children = results.getList("children");
        for (Object o : children) {
            GdbMiResults childResult = (GdbMiResults) o;
            GdbMiResults child = childResult.getTuple("child");
            String varName = child.getString("name");
            GdbVariableObject var = variableObjectMap.get(varName);
            if (null == var) {
                var = new GdbVariableObject(varName, null);
                variableObjectMap.put(varName, var);
            }
            var.updateFromResult(child);
            childrenList.add(varName.substring(varName.lastIndexOf('.') + 1), new PascalDebuggerValue(this, var.getExpression(), var.getType(), var.getValue(), var.getChildrenCount()));
        }
        lastParentNode.addChildren(childrenList, true);
        lastParentNode = null;
    }
}
