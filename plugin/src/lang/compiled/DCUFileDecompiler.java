package com.siberika.idea.pascal.lang.compiled;

import com.google.common.base.Joiner;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 21/05/2015
 */
public class DCUFileDecompiler implements BinaryFileDecompiler {
    private static final Logger LOG = Logger.getInstance(DCUFileDecompiler.class);

    private static final Pattern WARNING = Pattern.compile("Warning:.+- all imported names will be shown with unit names");
    private static final Pattern CONSTANT1 = Pattern.compile("\\s*\\d\\d:\\s*.+(\\||\\[)[A-F0-9 (]+\\|.*");
    private static final Pattern CONSTANT2 = Pattern.compile("\\s*raw\\s*\\[\\$[0-9A-F]+\\.\\.\\$[0-9A-F]+\\]\\s*at \\$[0-9A-F]+");
    private static final File NULL_FILE = new File("");

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
        File decompilerCommand = BasePascalSdkType.getDecompilerCommand(sdk, NULL_FILE);
        String result = "";
        try {
            if (!decompilerCommand.isFile() || !decompilerCommand.canExecute()) {
                return PascalBundle.message("decompile.wrong.delphi", decompilerCommand.getCanonicalPath());
            }
            List<String> paths = collectUnitPaths(sdk, module);
            result = SysUtils.runAndGetStdOut(sdk.getHomePath(), decompilerCommand.getCanonicalPath(), files.iterator().next().getPath(),
                    "-U" + Joiner.on(';').join(paths), "-I", "-");
            if (result != null) {
                return handleText(result);
            } else {
                return PascalBundle.message("decompile.empty.result");
            }
        } catch (IOException e) {
            LOG.info("I/O error: " + e.getMessage(), e);
            return PascalBundle.message("decompile.io.error");
        } catch (PascalException e1) {
            return e1.getMessage();
        } catch (Exception e) {
            LOG.info("Unknown error: " + e.getMessage(), e);
            return PascalBundle.message("decompile.unknown.error", result);
        }
    }

    private static List<String> collectUnitPaths(Sdk sdk, Module module) {
        VirtualFile[] sdkFiles = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);
        Set<File> paths = com.siberika.idea.pascal.jps.util.FileUtil.retrievePaths(sdkFiles);
        List<String> result = new ArrayList<String>(paths.size());
        for (File path : paths) {
            result.add(path.getAbsolutePath());
            LOG.info("===*** path: " + path.getAbsolutePath());
        }
        return result;
    }

    private static String handleText(@NotNull String result) {
        String[] lines = result.split("\n");
        boolean unitDone = false;
        boolean inConst = false;
        StringBuilder res = new StringBuilder();
        for (String line : lines) {
            if (isConstant(line)) {                               // Comment out all non-compilable constant declarations
                if (!inConst) {
                    res.append("    default;\n");                      // insert const value
                    inConst = true;
                }
                res.append("// ");
            } else {
                inConst = false;
            }
            if (isWarning(line)) {                         // Comment out all decompiler warnings
                res.append("// ");
            } else if (!unitDone) {                               // Comment out all lines before unit declarations
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

    private static boolean isConstant(String line) {
        return CONSTANT1.matcher(line).matches() || CONSTANT2.matcher(line).matches();
    }

    private static boolean isWarning(String line) {
        return WARNING.matcher(line).matches();
    }

}
