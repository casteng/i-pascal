package com.siberika.idea.pascal.run;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.siberika.idea.pascal.PascalAppService;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.module.PascalModuleType;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class PascalBeforeRunProvider extends BeforeRunTaskProvider<PascalBeforeRunProvider.PrepareBuildBeforeRunTask> {

    private static final Key<PrepareBuildBeforeRunTask> ID = Key.create("PasPrepareRunBuild");
    private static final String[] EXECUTABLE_EXTENSIONS = {null, "exe", "EXE"};
    private static final String[] PROGRAM_EXTENSIONS = {"dpr", "lpr", "pas", "pp"};

    @Override
    public Key<PrepareBuildBeforeRunTask> getId() {
        return ID;
    }

    @Override
    public String getName() {
        return PascalBundle.message("before.launch.prepare.build");
    }

    @Nullable
    @Override
    public PrepareBuildBeforeRunTask createTask(@NotNull RunConfiguration configuration) {
        PrepareBuildBeforeRunTask task = null;
        if (configuration instanceof PascalRunConfiguration) {
            task = new PrepareBuildBeforeRunTask();
            task.setEnabled(true);
        }
        return task;
    }

    @Override
    public boolean canExecuteTask(@NotNull RunConfiguration configuration, @NotNull PrepareBuildBeforeRunTask task) {
        return configuration instanceof PascalRunConfiguration;
    }

    @Override
    public boolean executeTask(DataContext context, @NotNull RunConfiguration configuration, @NotNull ExecutionEnvironment env, @NotNull PrepareBuildBeforeRunTask task) {
        Executor executor = env.getExecutor();
        if (configuration instanceof PascalRunConfiguration) {
            ((PascalRunConfiguration) configuration).setDebugMode(executor instanceof DefaultDebugExecutor);
            setMainFileIfNeeded(configuration, env);
            deleteExecutables(((PascalRunConfiguration) configuration).findModule(env), configuration);
        }
        return true;
    }

    private void setMainFileIfNeeded(RunConfiguration configuration, ExecutionEnvironment env) {
        String programName = ((PascalRunConfiguration) configuration).getProgramFileName();
        Module module = programName != null ? ((PascalRunConfiguration) configuration).findModule(env) : null;
        if (module != null) {
            programName = FileUtilRt.getNameWithoutExtension(programName);
            VirtualFile existingMainFile = PascalModuleType.getMainFile(module);
            if ((existingMainFile == null) || !programName.equalsIgnoreCase(existingMainFile.getNameWithoutExtension())) {
                VirtualFile mainFile = findMainFile(configuration.getProject(), module, programName);
                if (mainFile != null) {
                    PascalModuleType.setMainFile(module, mainFile);
                    CompilerModuleExtension moduleExtension = CompilerModuleExtension.getInstance(module);
                    moduleExtension.commit();
                }
            }
        }
    }

    private VirtualFile findMainFile(Project project, Module module, String programName) {
        return ApplicationManager.getApplication().runReadAction(
                new Computable<VirtualFile>() {
                    @Override
                    public VirtualFile compute() {
                        List<VirtualFile> files = ModuleUtil.findFileByName(project, programName, GlobalSearchScope.moduleWithDependenciesScope(module), PROGRAM_EXTENSIONS);
                        for (VirtualFile file : files) {
                            PsiFile psi = PsiManager.getInstance(project).findFile(file);
                            PasModule pasModule = psi != null ? PsiUtil.getElementPasModule(psi) : null;
                            if (pasModule != null && pasModule.getModuleType() == PascalModule.ModuleType.PROGRAM) {
                                return file;
                            }
                        }
                        return null;
                    }
                }
        );
    }

    private void deleteExecutables(Module module, RunConfiguration configuration) {
        String programName = ((PascalRunConfiguration) configuration).getProgramFileName();
        CompilerModuleExtension moduleExtension = CompilerModuleExtension.getInstance(module);
        VirtualFile outputPath = moduleExtension != null ? moduleExtension.getCompilerOutputPath() : null;
        String exeOutputPath = module.getOptionValue(JpsPascalModuleType.USERDATA_KEY_EXE_OUTPUT_PATH.toString());
        exeOutputPath = StringUtil.isEmpty(exeOutputPath) ? (outputPath != null ? outputPath.getPath() : null) : exeOutputPath;
        if (exeOutputPath != null) {
            File exeDir = new File(exeOutputPath);
            for (String ext : EXECUTABLE_EXTENSIONS) {        // Delete executables
                File exe = new File(exeDir, programName + ((ext != null ? ("." + ext) : "")));
                if (exe.isFile()) {
                    if (!exe.delete()) {
                        Notifications.Bus.notify(new Notification(PascalAppService.PASCAL_NOTIFICATION_GROUP, PascalBundle.message("before.run.title"),
                                PascalBundle.message("before.run.delete.fail", exe.getAbsolutePath()), NotificationType.WARNING));
                    }
                }
            }
        }
    }

    static class PrepareBuildBeforeRunTask extends BeforeRunTask<PrepareBuildBeforeRunTask> {
        PrepareBuildBeforeRunTask() {
            super(ID);
            setEnabled(true);
        }
    }

}
