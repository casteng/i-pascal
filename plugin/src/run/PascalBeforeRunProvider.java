package com.siberika.idea.pascal.run;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.util.Key;
import com.siberika.idea.pascal.PascalBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalBeforeRunProvider extends BeforeRunTaskProvider<PascalBeforeRunProvider.PrepareBuildBeforeRunTask> {

    private static final Key<PrepareBuildBeforeRunTask> ID = Key.create("PasPrepareRunBuild");

    @Override
    public Key<PascalBeforeRunProvider.PrepareBuildBeforeRunTask> getId() {
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
        }
        return true;
    }

    static class PrepareBuildBeforeRunTask extends BeforeRunTask<PrepareBuildBeforeRunTask> {
        PrepareBuildBeforeRunTask() {
            super(ID);
            setEnabled(true);
        }
    }

}
