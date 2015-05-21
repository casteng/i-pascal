package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.DelphiSdkType;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.SysUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 21/05/2015
 */
public class DCUFileDecompiler implements BinaryFileDecompiler {
    private static final Logger LOG = Logger.getInstance(DCUFileDecompiler.class);

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile file) {
        assert file.getFileType() == DCUFileType.INSTANCE;

        final Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return "";
        final Project project = projects[0];

        return decompileText(file.getPath(), ModuleUtil.getModuleForFile(project, file));
    }

    public static String decompileText(String filename, Module module) {
        if (null == module) {
            return PascalBundle.message("decompile.no.module", filename);
        }
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (null == sdk) {
            return PascalBundle.message("decompile.wrong.sdk");
        }
        if ((sdk.getHomePath() == null) || !(sdk.getSdkType() instanceof DelphiSdkType)) {
            return PascalBundle.message("decompile.wrong.sdktype.delphi");
        }
        String unitName = FileUtil.getNameWithoutExtension(com.siberika.idea.pascal.jps.util.FileUtil.getFilename(filename));
        Collection<VirtualFile> files = ModuleUtil.getAllCompiledModuleFilesByName(module, unitName, DCUFileType.INSTANCE);
        if (files.isEmpty()) {
            return PascalBundle.message("decompile.file.notfound", filename);
        }
        File decompilerCommand = BasePascalSdkType.getDecompilerCommand(sdk);
        String result = "";
        try {
            if (!decompilerCommand.isFile() || !decompilerCommand.canExecute()) {
                return PascalBundle.message("decompile.wrong.ppudump", decompilerCommand.getCanonicalPath());
            }
            result = SysUtils.runAndGetStdOut(sdk.getHomePath(), decompilerCommand.getCanonicalPath(), files.iterator().next().getPath(), "-I", "-");
            if (result != null) {
                return handleText(result);
            } else {
                return PascalBundle.message("decompile.empty.result");
            }
        } catch (IOException e) {
            LOG.warn("I/O error: " + e.getMessage(), e);
            return PascalBundle.message("decompile.io.error");
        } catch (PascalException e1) {
            return e1.getMessage();
        } catch (Exception e) {
            LOG.warn("Unknown error: " + e.getMessage(), e);
            return PascalBundle.message("decompile.unknown.error", result);
        }
    }

    private static String handleText(@NotNull String result) {
        String[] lines = result.split("\n");
        boolean unitDone = false;
        StringBuilder res = new StringBuilder();
        for (String line : lines) {
            if (!unitDone) {                                // Comment out all lines before unit declarations
                if (line.startsWith("unit")) {
                    unitDone = true;
                } else {
                    res.append("// ");
                }
            }
            if (!line.startsWith("procedure Finalization")) {
                res.append(line).append("\n");
            }
        }
        res.append("implementation\n  {compiled code}\nend.\n");
        return res.toString();
    }

}
