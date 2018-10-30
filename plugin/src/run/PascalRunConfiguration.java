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
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.SearchScopeProvider;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.search.GlobalSearchScope;
import com.siberika.idea.pascal.module.PascalModuleType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 06/01/2013
 */
public class PascalRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
        implements PascalRunConfigurationParams, RunConfigurationWithSuppressedDefaultRunAction, RunConfigurationWithSuppressedDefaultDebugAction {

    private static final String ATTR_PROGRAM_FILE_NAME = "program_file_name";

    private String parameters;
    private String workingDirectory;
    private String programFileName;
    private boolean fixIOBuffering = true;
    private boolean debugMode = false;

    public PascalRunConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory) {
        super(name, configurationModule, factory);
        workingDirectory = getProject().getBasePath();
    }

    @Override
    public Collection<Module> getValidModules() {
        return getAllModules();
    }

    @Override
    protected ModuleBasedConfiguration createInstance() {
        workingDirectory = getProject().getBasePath();
        return new PascalRunConfiguration(getName(),  getConfigurationModule(),  getFactory());
    }

    @NotNull
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new PascalRunConfigurationEditor(this);
    }

    Module findModule(@NotNull ExecutionEnvironment env) {
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
        return new PascalCommandLineState(this, env, executor instanceof DefaultDebugExecutor, workingDirectory, parameters, fixIOBuffering);
    }

    public static void copyParams(PascalRunConfigurationParams from, PascalRunConfigurationParams to) {
        to.setParameters(from.getParameters());
        to.setWorkingDirectory(from.getWorkingDirectory());
        to.setFixIOBuffering(from.getFixIOBuffering());
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
    public boolean getFixIOBuffering() {
        return fixIOBuffering;
    }

    @Override
    public boolean getDebugMode() {
        return debugMode;
    }

    @Override
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public void setFixIOBuffering(boolean value) {
        fixIOBuffering = value;
    }

    @Override
    public void setDebugMode(boolean value) {
        debugMode = value;
    }

    public String getProgramFileName() {
        return programFileName;
    }

    public void setProgramFileName(String programFileName) {
        this.programFileName = programFileName;
    }

    // 2016.3 compatibility
    public GlobalSearchScope getSearchScope() {
        return SearchScopeProvider.createSearchScope(getModules());
    }

    public Sdk getSdk() {
        return getConfigurationModule().getModule() != null ?
                ModuleRootManager.getInstance(getConfigurationModule().getModule()).getSdk() :
                ProjectRootManager.getInstance(getProject()).getProjectSdk();
    }

    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        setProgramFileName(element.getAttributeValue(ATTR_PROGRAM_FILE_NAME));
    }

    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        if (programFileName != null) {
            element.setAttribute(ATTR_PROGRAM_FILE_NAME, programFileName);
        }
    }

    @Override
    public String toString() {
        return (debugMode ? "[debug]" : "[]") + super.toString();
    }
}