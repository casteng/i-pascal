/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.siberika.idea.pascal.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.module.PascalModuleType;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 06/01/2013
 */
public class PascalRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
        implements PascalRunConfigurationParams, RunConfigurationWithSuppressedDefaultRunAction, RunConfigurationWithSuppressedDefaultDebugAction {
    static Logger log = Logger.getLogger(PascalRunConfiguration.class);
    private String parameters;
    private String workingDirectory;

    public PascalRunConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory) {
        super(name, configurationModule, factory);
        workingDirectory = getProject() != null ? getProject().getBasePath() : null;
    }

    @Override
    public Collection<Module> getValidModules() {
        return getAllModules();
    }

    @Override
    protected ModuleBasedConfiguration createInstance() {
        workingDirectory = getProject() != null ? getProject().getBasePath() : null;
        return new PascalRunConfiguration(getName(),  getConfigurationModule(),  getFactory());
    }

    @NotNull
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new PascalRunConfigurationEditor(this);
    }

    private Module findModule(@NotNull ExecutionEnvironment env) {
        Module result = null;
        if ((env.getRunnerAndConfigurationSettings() != null) &&
            (env.getRunnerAndConfigurationSettings().getConfiguration() instanceof PascalRunConfiguration)) {
            PascalRunConfiguration configuration = (PascalRunConfiguration) env.getRunnerAndConfigurationSettings().getConfiguration();
            result = configuration.getConfigurationModule().getModule();
        }
        if (null == result) {
            for (Module module : getValidModules()) {
                if (PascalModuleType.isPascalModule(module)) {
                    return module;
                }
            }
        }
        return result;
    }

    @Nullable
    public RunProfileState getState(@NotNull Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
        final String workDirectory = this.workingDirectory;
        final List<String> params = new ArrayList<String>();
        if ((parameters != null) && (parameters.length() > 0)) {
            params.addAll(Arrays.asList(parameters.split("\\s+"))); //TODO: use exec*utils to correctly split params
        }
        return new CommandLineState(env) {
            @NotNull
            @Override
            protected ProcessHandler startProcess() throws ExecutionException {
                Module module = findModule(env);
                GeneralCommandLine commandLine = new GeneralCommandLine();
                String executable = PascalRunner.getExecutable(module);
                if (executable != null) {
                    commandLine.setExePath(executable);
                } else {
                    throw new ExecutionException(PascalBundle.message("execution.noExecutable"));
                }
                commandLine.addParameters(params);
                commandLine.setWorkDirectory(workDirectory);
                ProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(), commandLine.getCharset(), commandLine.getCommandLineString());
                setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
                return handler;
            }
        };
    }

    public static void copyParams(PascalRunConfigurationParams from, PascalRunConfigurationParams to) {
        to.setParameters(from.getParameters());
        to.setWorkingDirectory(from.getWorkingDirectory());
    }

    @Override
    public String getParameters() {
        return parameters;
    }

    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}