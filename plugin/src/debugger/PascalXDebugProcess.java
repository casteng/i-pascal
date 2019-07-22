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
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.debugger.gdb.GdbStackFrame;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;
import com.siberika.idea.pascal.editor.ContextAwareVirtualFile;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.run.PascalRunConfiguration;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public abstract class PascalXDebugProcess extends XDebugProcess {

    private static final Logger LOG = Logger.getInstance(PascalXDebugProcess.class);

    public final Options options = new Options();

    private static final AnAction[] EMPTY_ACTIONS = new AnAction[0];

    protected final ExecutionResult executionResult;

    protected ConsoleView console;
    private LogConsoleImpl outputConsole;
    protected File outputFile;
    ExecutionEnvironment environment;
    protected Sdk sdk;

    private final XBreakpointHandler<?>[] MY_BREAKPOINT_HANDLERS = new XBreakpointHandler[] {new PascalLineBreakpointHandler(this)};

    private boolean inferiorRunning = false;

    public abstract String getVarFrame();
    public abstract String getVarNameQuoteChar();

    private CommandSender sender;

    protected abstract void init();

    public PascalXDebugProcess(XDebugSession session, ExecutionEnvironment environment, ExecutionResult executionResult) {
        super(session);
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
        sender.send(command, null, null);
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

    // handling of -var-create command
    public void handleVarResult(GdbMiResults res) {
        XStackFrame frame = getCurrentFrame();
        if (frame instanceof GdbStackFrame) {
            ((GdbStackFrame) frame).createOrUpdateVar(res, false);
            ((GdbStackFrame) frame).refreshVariablesUI();
        }
    }

    // handling of -var-update command
    public void handleVarUpdate(GdbMiResults results) {
        XStackFrame frame = getCurrentFrame();
        if (frame instanceof GdbStackFrame) {
            List<Object> changes = results.getList("changelist");
            for (Object o : changes) {
                GdbMiResults change = (GdbMiResults) o;
                ((GdbStackFrame) frame).createOrUpdateVar(change, false);
            }
            ((GdbStackFrame) frame).refreshVariablesUI();
        }
    }

    // handling of LLDB fr v
    public void handleVarUpdate(String varKey, String type, String value) {
        XStackFrame frame = getCurrentFrame();
        if (frame instanceof GdbStackFrame) {
            GdbMiResults res = new GdbMiResults();
            res.setValue("name", varKey);
            res.setValue("type", type);
            res.setValue("value", value);
            ((GdbStackFrame) frame).createOrUpdateVar(res, false);
            ((GdbStackFrame) frame).refreshVariablesUI();
        }
    }

    public void removeVariable(String varKey) {
        XStackFrame frame = getCurrentFrame();
        if (frame instanceof GdbStackFrame) {
            ((GdbStackFrame) frame).removeVar(varKey);
        }
    }

    // handling of -stack-list-variables command
    synchronized public void handleVariablesResponse(List<Object> variables, CommandSender.FinishCallback callback) {
        XStackFrame frame = getCurrentFrame();
        if (frame instanceof GdbStackFrame) {
            ((GdbStackFrame) frame).clearVars();
            for (Object o : variables) {
                if (o instanceof GdbMiResults) {
                    GdbMiResults res = (GdbMiResults) o;
                    ((GdbStackFrame) frame).createOrUpdateVar(res, true);
                } else {
                    LOG.error(String.format("Invalid variables list entry: %s", o));
                    return;
                }
            }
            node.addChildren(childrenList, true);
        }
    }

    private XStackFrame getCurrentFrame() {
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
