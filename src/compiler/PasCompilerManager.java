package com.siberika.idea.pascal.compiler;

import com.intellij.openapi.compiler.Compiler;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 1/6/13
 */
public class PasCompilerManager implements ProjectComponent {
    private final CompilerManager compilerManager;
    private final Compiler compiler;

    public PasCompilerManager(CompilerManager compilerManager, Project project) {
        this.compilerManager = compilerManager;
        this.compiler = new PasFPCCompiler(project);
    }

    @NotNull
    @NonNls
    public String getComponentName() {
        return "PascalIDEACompilerManager";
    }

    public void projectOpened() {
        compilerManager.addCompiler(compiler);
        //compilerManager.addCompilableFileType(PascalFileType.INSTANCE);
    }

    public void projectClosed() {
        //compilerManager.removeCompilableFileType(PascalFileType.INSTANCE);
        compilerManager.removeCompiler(compiler);
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }
}
