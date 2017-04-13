package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.diagnostic.logging.LogConsoleImpl;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.content.Content;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.debugger.PascalDebuggerValue;
import com.siberika.idea.pascal.debugger.PascalLineBreakpointHandler;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.run.PascalRunConfiguration;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class GdbXDebugProcess extends XDebugProcess {

    private static final Logger LOG = Logger.getInstance(GdbXDebugProcess.class);

    private final XBreakpointHandler<?>[] MY_BREAKPOINT_HANDLERS = new XBreakpointHandler[] {new PascalLineBreakpointHandler(this)};
    private final ExecutionResult executionResult;
    private ConsoleView console;
    private LogConsoleImpl outputConsole;
    private XCompositeNode lastQueriedVariablesCompositeNode;
    private XCompositeNode lastParentNode;
    private Map<String, GdbVariableObject> variableObjectMap;

    private static final String VAR_PREFIX_LOCAL = "l%";
    private static final String VAR_PREFIX_WATCHES = "w%";
    private static final AnAction[] EMPTY_ACTIONS = new AnAction[0];
    private boolean inferiorRunning = false;
    private File outputFile;
    private Sdk sdk;
    Options options = new Options();

    public GdbXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        super(session);
        RunProfile conf = environment.getRunProfile();
        if (conf instanceof PascalRunConfiguration) {
            Module module = ((PascalRunConfiguration) conf).getConfigurationModule().getModule();
            sdk = module != null ? ModuleRootManager.getInstance(module).getSdk() : null;
        }
        if (null == sdk) {
            sdk = ProjectRootManager.getInstance(environment.getProject()).getProjectSdk();
        }

        this.executionResult = executionResult;
        try {
            createGdbProcess(environment);
        } catch (ExecutionException e) {
            LOG.warn("Error running GDB", e);
        }
    }

    private void createGdbProcess(ExecutionEnvironment env) throws ExecutionException {
        if (isOutputConsoleNeeded()) {
            createOutputConsole(env.getProject());
        }
        console = (ConsoleView) executionResult.getExecutionConsole();
        variableObjectMap = new HashMap<String, GdbVariableObject>();
        sendCommand("-break-delete");
    }

    private boolean isOutputConsoleNeeded() {
        return !SystemInfo.isWindows && getData().getBoolean(PascalSdkData.Keys.DEBUGGER_REDIRECT_CONSOLE);
    }

    private void createOutputConsole(Project project) {
        try {
            outputFile = File.createTempFile("ipas_run_out_", ".tmp");
            outputConsole = new LogConsoleImpl(project, outputFile, Charset.forName("utf-8"), 0,
                    PascalBundle.message("debug.output.title"), false, GlobalSearchScope.allScope(project)) {
                @Override
                public boolean isActive() {
                    return true;
                }
            };
            outputConsole.activate();
        } catch (IOException e) {
            LOG.warn("Error creating output console");
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
        return console;
    }

    public void printToConsole(String text, ConsoleViewContentType contentType) {
        if (console != null) {
            console.print(text, contentType);
        }
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
        getProcessHandler().addProcessListener(new GdbProcessAdapter(this));
        sendCommand("-gdb-set target-async on");
        if (getData().getBoolean(PascalSdkData.Keys.DEBUGGER_REDIRECT_CONSOLE)) {
            if (SystemInfo.isWindows) {
                sendCommand("-gdb-set new-console on");
            } else {
                sendCommand("-exec-arguments > " + outputFile.getAbsolutePath());
            }
        } 
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
        if (getSession().isStopped()) {
            return;
        }
        try {
            OutputStream commandStream = getProcessHandler().getProcessInput();
            if (commandStream != null) {
                commandStream.write((command + "\n").getBytes("UTF-8"));
                commandStream.flush();
                printToConsole(">>>> " + command + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
            }
        } catch (IOException e) {
            LOG.warn("ERROR: sending command to GDB", e);
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
    public XDebugTabLayouter createTabLayouter() {
        return new XDebugTabLayouter() {
            @Override
            public void registerAdditionalContent(@NotNull RunnerLayoutUi ui) {
                if (!isOutputConsoleNeeded()) {
                    return;
                }
                Content gdbConsoleContent = ui.createContent("PascalDebugConsoleContent", outputConsole.getComponent(),
                        PascalBundle.message("debug.output.title"), AllIcons.Debugger.Console, outputConsole.getPreferredFocusableComponent());
                gdbConsoleContent.setCloseable(false);

                DefaultActionGroup consoleActions = new DefaultActionGroup();
                AnAction[] actions = outputConsole.getConsole() != null ? outputConsole.getConsole().createConsoleActions() : EMPTY_ACTIONS;
                for (AnAction action : actions) {
                    consoleActions.add(action);
                }
                gdbConsoleContent.setActions(consoleActions, ActionPlaces.DEBUGGER_TOOLBAR, outputConsole.getPreferredFocusableComponent());

                ui.addContent(gdbConsoleContent, 2, PlaceInGrid.bottom, false);
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
                    if (varName.startsWith(VAR_PREFIX_LOCAL) || varName.startsWith(VAR_PREFIX_WATCHES)) {
                        varName = varName.substring(2);
                    }
                    PasField.FieldType fieldType = PasField.FieldType.VARIABLE;
                    XStackFrame frame = getSession().getCurrentStackFrame();
                    if (frame instanceof GdbStackFrame) {
                        PasField field = ((GdbStackFrame) frame).resolveIdentifierName(varName, PasField.TYPES_LOCAL);
                        if (field != null) {
                            varName = formatVariableName(field);
                            fieldType = field.fieldType;
                        }
                    }
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
                            new PascalDebuggerValue(this, var.getKey(), var.getType(), var.getValue(), var.getChildrenCount(), fieldType));
                } else {
                    node.setErrorMessage("Invalid variables list entry");
                    return;
                }
            }
            node.addChildren(childrenList, true);
        }
    }

    private String formatVariableName(@NotNull PasField field) {
        return field.name + (field.fieldType == PasField.FieldType.ROUTINE ? "()" : "");
    }

    public boolean isInferiorRunning() {
        return inferiorRunning;
    }

    public void setInferiorRunning(boolean inferiorRunning) {
        this.inferiorRunning = inferiorRunning;
    }

    public PascalSdkData getData() {
        return sdk != null ? BasePascalSdkType.getAdditionalData(sdk) : PascalSdkData.EMPTY;
    }

    final class Options {
        boolean resolveNames() {
            return getData().getBoolean(PascalSdkData.Keys.DEBUGGER_RESOLVE_NAMES);
        }
        boolean callGetters() {
            return false;
        }
        String asmFormat() {
            return getData().getString(PascalSdkData.Keys.DEBUGGER_ASM_FORMAT);
        }

        public boolean needPosition() {
            return resolveNames() || callGetters();
        }
    }
}
