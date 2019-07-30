package com.siberika.idea.pascal.debugger;

import com.intellij.diagnostic.logging.LogConsoleImpl;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
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
import com.intellij.openapi.ui.MessageType;
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
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.debugger.gdb.GdbExecutionStack;
import com.siberika.idea.pascal.debugger.gdb.GdbStackFrame;
import com.siberika.idea.pascal.debugger.gdb.GdbSuspendContext;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbStopReason;
import com.siberika.idea.pascal.editor.ContextAwareVirtualFile;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.run.PascalRunConfiguration;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PascalXDebugProcess extends XDebugProcess {

    private static final Logger LOG = Logger.getInstance(PascalXDebugProcess.class);

    private static final Pattern PATTERN_VAR_UPDATE_FAILED = Pattern.compile("\\w+ 'var-update'\\. Variable '(.+)' does not exist");
    private static final AnAction[] EMPTY_ACTIONS = new AnAction[0];

    public final Options options = new Options();

    protected final ExecutionResult executionResult;

    protected ConsoleView console;
    private LogConsoleImpl outputConsole;
    protected File outputFile;
    ExecutionEnvironment environment;
    protected Sdk sdk;

    private final XBreakpointHandler<?>[] MY_BREAKPOINT_HANDLERS = new XBreakpointHandler[] {new PascalLineBreakpointHandler(this)};

    private boolean inferiorRunning = false;
    private GdbSuspendContext suspendContext;
    private final VariableManager variableManager;

    public abstract String getVarFrame();
    public abstract String getVarNameQuoteChar();

    private CommandSender sender;

    protected abstract void init();

    public PascalXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        super(session);
        this.variableManager = new VariableManager(this);
        this.environment = environment;
        this.sdk = retrieveSdk(environment);
        this.executionResult = executionResult;
        this.sender = new CommandSender(this);
        this.sender.start();
        try {
            init();
        } catch (Exception e) {
            LOG.warn("Error launching debug process", e);
        }
    }

    @Override
    protected synchronized void finalize() throws Throwable {
        super.finalize();
        LOG.info("Terminating sender in finalize");
        terminateSender();
    }

    private void terminateSender() {
        if (sender != null && sender.isAlive()) {
            sender.interrupt();
            try {
                sender.join(200);
            } catch (InterruptedException e) {
                LOG.info("Interrupted while terminating sender thread", e);
            }
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
        LOG.info("Terminating sender");
        terminateSender();
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        sendCommand(String.format("-exec-until \"%s:%d\"", position.getFile().getCanonicalPath(), position.getLine()));
    }

    public void sendCommand(String command) {
        sender.send(command, null);
    }

    void sendCommand(String command, CommandSender.FinishCallback callback) {
        sender.send(command, callback);
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

    public void handleResponse(GdbMiLine res) {
//        LOG.info("DBG: " + res);
        if (GdbMiLine.Type.EXEC_ASYNC.equals(res.getType())) {
            if ("stopped".equals(res.getRecClass())) {
                handleStop(res);
            } else if ("running".equals(res.getRecClass())) {
                setInferiorRunning(true);
            }
        } else if (GdbMiLine.Type.RESULT_RECORD.equals(res.getType())) {
            if ("done".equals(res.getRecClass())) {
                if (res.getResults().getValue("stack") != null) {                   // -stack-list-frames result
                    addStackFramesToContainer(res.getResults().getList("stack"));
                    queryVariables();
                } else if (res.getResults().getValue("bkpt") != null) {
                    getBreakpointHandler().handleBreakpointResult(res.getResults().getTuple("bkpt"));
                } else if (isCreateVarResult(res.getResults())) {
                    variableManager.handleVarResult(res.getResults());
                } else if (res.getResults().getValue("changelist") != null) {
                    variableManager.handleVarUpdate(res.getResults());
                }
            } else if ("error".equals(res.getRecClass())) {
                LOG.info(String.format("Debugger error: %s", res));
                String msg = res.getResults().getString("msg");
                if ((msg != null) && !handleError(msg)) {
                    getSession().reportMessage(PascalBundle.message("debug.error.response",
                            msg.replace("\\n", "\n")), MessageType.ERROR);
                }
            }
//        } else if (!"(gdb)\n".equals(text)) {
        }
    }

    private void queryVariables() {
        XStackFrame frame = getCurrentFrame();
        if (frame instanceof GdbStackFrame) {
            ((GdbStackFrame) frame).queryVariables();
        }
    }

    XStackFrame getCurrentFrame() {
        XDebugSession session = getSession();
        return session != null ? session.getCurrentStackFrame() : null;
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

    public CommandSender.FinishCallback findCallback(GdbMiLine res) {
        return sender.findCallback(res.getToken());
    }

    private boolean handleError(String msg) {
        Matcher matcher = PATTERN_VAR_UPDATE_FAILED.matcher(msg);
        if (matcher.matches()) {
            variableManager.removeVariable(matcher.group(1));
            return true;
        }
        return false;
    }

    private boolean isCreateVarResult(GdbMiResults results) {
        return (results.getValue("name") != null) && (results.getValue("value") != null);
    }

    private void handleStop(GdbMiLine res) {
        suspendContext = new GdbSuspendContext(this, res);
        setInferiorRunning(false);
        getSession().positionReached(suspendContext);
        GdbStopReason reason = GdbStopReason.fromUid(res.getResults().getString("reason"));
        String msg = null;
        MessageType messageType = MessageType.INFO;
        if (reason != null) {
            switch (reason) {
                case SIGNAL_RECEIVED: {
                    String detail = res.getResults().getString("signal-name");
                    msg = detail != null ? String.format(", %s (%s)", res.getResults().getValue("signal-name"), res.getResults().getValue("signal-meaning")) : "";
                    break;
                }
                case BREAKPOINT_HIT:
                case WATCHPOINT_TRIGGER:
                case READ_WATCHPOINT_TRIGGER:
                case ACCESS_WATCHPOINT_TRIGGER:
                case LOCATION_REACHED:
                case FUNCTION_FINISHED: {
                    msg = reason.getUid();
                    messageType = MessageType.WARNING;
                    break;
                }
                case EXITED:
                case EXITED_SIGNALLED:
                case EXITED_NORMALLY: {
                    msg = reason.getUid();
                    sendCommand("-gdb-exit");
                    break;
                }
                case EXCEPTION: {
                    msg = reason.getUid() + ": " + res.getResults().getString("exception");
                    messageType = MessageType.ERROR;
                    break;
                }
            }
            if (msg != null) {
                getSession().reportMessage(PascalBundle.message("debug.notify.stopped", msg), messageType);
            }
        }
    }

    private void addStackFramesToContainer(List<Object> stack) {
        List<XStackFrame> frames = new ArrayList<>();
        for (Object o : stack) {
            if (o instanceof GdbMiResults) {
                GdbMiResults res = (GdbMiResults) o;
                frames.add(new GdbStackFrame((GdbExecutionStack) suspendContext.getActiveExecutionStack(), res.getTuple("frame")));
            } else {
                reportError("Invalid stack frames list entry");
                return;
            }
        }
        ((GdbExecutionStack) suspendContext.getActiveExecutionStack()).addStackFrames(frames);
    }

    private void reportError(String msg) {
        LOG.warn("ERROR: " + msg);
    }

    public VariableManager getVariableManager() {
        return variableManager;
    }

    void syncCalls(int levels, CommandSender.FinishCallback callback) {
        sender.syncCalls(levels, callback);
    }

    public final class Options {
        public boolean supportsBulkDelete;
        public boolean showNonPrintable = true;
        public int limitChars = 10;
        public int limitElements = 10;
        public int limitChilds = 10;

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
