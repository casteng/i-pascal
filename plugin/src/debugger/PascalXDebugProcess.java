package com.siberika.idea.pascal.debugger;

import com.intellij.diagnostic.logging.LogConsoleImpl;
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
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.content.Content;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerUtil;
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
import com.siberika.idea.pascal.debugger.gdb.GdbStackFrame;
import com.siberika.idea.pascal.debugger.gdb.GdbVariableObject;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.editor.ContextAwareVirtualFile;
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
import java.util.List;
import java.util.Map;

public abstract class PascalXDebugProcess extends XDebugProcess {

    private static final Logger LOG = Logger.getInstance(PascalXDebugProcess.class);

    private final String VAR_FRAME = getVarFrame();

    private final String VAR_NAME_QUOTE_CHAR = getVarNameQuoteChar();

    public final Options options = new Options();

    protected static final AnAction[] EMPTY_ACTIONS = new AnAction[0];

    protected final ExecutionResult executionResult;

    protected ConsoleView console;
    protected LogConsoleImpl outputConsole;
    protected Map<String, GdbVariableObject> variableObjectMap;
    protected File outputFile;
    protected ExecutionEnvironment environment;
    protected Sdk sdk;
    private static final String VAR_PREFIX_LOCAL = "l%";

    private static final String VAR_PREFIX_WATCHES = "w%";
    private final XBreakpointHandler<?>[] MY_BREAKPOINT_HANDLERS = new XBreakpointHandler[] {new PascalLineBreakpointHandler(this)};

    private XCompositeNode lastQueriedVariablesCompositeNode;
    private XCompositeNode lastParentNode;
    private boolean inferiorRunning = false;

    protected abstract String getVarFrame();
    protected abstract String getVarNameQuoteChar();

    protected abstract void init();

    public PascalXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        super(session);
        this.environment = environment;
        this.sdk = retrieveSdk(environment);
        this.executionResult = executionResult;
        try {
            init();
        } catch (Exception e) {
            LOG.warn("Error launching debug process", e);
        }
    }

    static Sdk retrieveSdk(ExecutionEnvironment environment) {
        Sdk sdk = null;
        RunProfile conf = environment.getRunProfile();
        if (conf instanceof PascalRunConfiguration) {
            Module module = ((PascalRunConfiguration) conf).getConfigurationModule().getModule();
            sdk = module != null ? ModuleRootManager.getInstance(module).getSdk() : null;
        } else {
            LOG.warn("Invalid run configuration class: " + (conf != null ? conf.getClass().getName() : "<null>"));
        }
        return sdk != null ? sdk : ProjectRootManager.getInstance(environment.getProject()).getProjectSdk();
    }

    protected void createOutputConsole() {
        try {
            outputFile = File.createTempFile("ipas_run_out_", ".tmp");
            outputConsole = new LogConsoleImpl(environment.getProject(), outputFile, Charset.forName("utf-8"), 0,
                    PascalBundle.message("debug.output.title"), false, GlobalSearchScope.allScope(environment.getProject())) {
                @Override
                public boolean isActive() {
                    return true;
                }
            };
            outputConsole.activate();
        } catch (Exception e) {
            LOG.warn("Error creating output console", e);
        }
    }

    protected boolean isOutputConsoleNeeded() {
        return !SystemInfo.isWindows && getData().getBoolean(PascalSdkData.Keys.DEBUGGER_REDIRECT_CONSOLE);
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
        sendCommand(String.format("-exec-until \"%s:%d\"", position.getFile().getCanonicalPath(), position.getLine()));
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
                LightVirtualFile file;
                if (sourcePosition != null) {
                    PsiElement psiElement = XDebuggerUtil.getInstance().findContextElement(sourcePosition.getFile(), sourcePosition.getOffset(), project, false);
                    file = new ContextAwareVirtualFile("_debug.pas", text, psiElement);
                } else {
                    file = new LightVirtualFile("_debug.pas", text);
                }
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
        String key = VAR_PREFIX_WATCHES + expression.replace(' ', '_');
        GdbVariableObject var = variableObjectMap.get(key);
        if (null == var) {
            variableObjectMap.put(key, new GdbVariableObject(key, expression, callback));
            sendCommand(String.format("-var-create %4$s%s%4$s %s \"%s\"", key, VAR_FRAME, expression, VAR_NAME_QUOTE_CHAR));
        } else {
            var.setCallback(callback);
            updateVariableObjectUI(var);
            sendCommand(String.format("-var-update --all-values %2$s%s%2$s", key, VAR_NAME_QUOTE_CHAR));
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

    synchronized public void handleVariablesResponse(List<Object> variables) {
        handleVariables(lastQueriedVariablesCompositeNode, variables, false);
    }

    synchronized public void handleChildrenResult(List<Object> variables) {
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
                    String varKey = (children ? "" : VAR_PREFIX_LOCAL) + varName.replace(' ', '_');
                    if (varName.startsWith(VAR_PREFIX_LOCAL) || varName.startsWith(VAR_PREFIX_WATCHES)) {
                        varName = varName.substring(2);
                    }
                    String varNameResolved = varName;
                    PasField.FieldType fieldType = PasField.FieldType.VARIABLE;
                    XStackFrame frame = getSession().getCurrentStackFrame();
                    if (frame instanceof GdbStackFrame) {
                        PasField field = ((GdbStackFrame) frame).resolveIdentifierName(varName, PasField.TYPES_LOCAL);
                        if (field != null) {
                            varNameResolved = formatVariableName(field);
                            fieldType = field.fieldType;
                        }
                    }
                    GdbVariableObject var = variableObjectMap.get(varKey);
                    if (null != var) {
                        var.updateFromResult(res);
                        if (!children) {
                            sendCommand(String.format("-var-update --all-values %2$s%s%2$s", varKey, VAR_NAME_QUOTE_CHAR));
                        }
                    } else {
                        var = new GdbVariableObject(varKey, varNameResolved, null, res);
                        variableObjectMap.put(varKey, var);
                        if (!children) {
                            sendCommand(String.format("-var-create %4$s%s%4$s %s \"%s\"", varKey, VAR_FRAME, varName, VAR_NAME_QUOTE_CHAR));
                        }
                    }

                    childrenList.add(varNameResolved.substring(varNameResolved.lastIndexOf('.')+1),
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

    boolean isInferiorRunning() {
        return inferiorRunning;
    }

    public void setInferiorRunning(boolean inferiorRunning) {
        this.inferiorRunning = inferiorRunning;
    }

    public PascalSdkData getData() {
        return getData(sdk);
    }

    public static PascalSdkData getData(Sdk sdk) {
        return sdk != null ? BasePascalSdkType.getAdditionalData(sdk) : PascalSdkData.EMPTY;
    }

    public final class Options {
        public boolean resolveNames() {
            return getData().getBoolean(PascalSdkData.Keys.DEBUGGER_RESOLVE_NAMES);
        }
        public boolean callGetters() {
            return false;
        }
        public String asmFormat() {
            return getData().getString(PascalSdkData.Keys.DEBUGGER_ASM_FORMAT);
        }

        public boolean needPosition() {
            return resolveNames() || callGetters();
        }
    }
}
