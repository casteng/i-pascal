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
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 21/05/2015
 */
public class DCUFileDecompiler implements BinaryFileDecompiler {
    private static final Logger LOG = Logger.getInstance(DCUFileDecompiler.class);

    private static final Pattern WARNING1 = Pattern.compile("Warning:.+- all imported names will be shown with unit names");
    private static final Pattern WARNING2 = Pattern.compile("Warning at 0x[A-F0-9]+.*");
    private static final Pattern CONSTANT1 = Pattern.compile("\\s*[A-F0-9]+:\\s*.+([|\\[])[A-F0-9 (]+\\|.*");
    private static final Pattern CONSTANT2 = Pattern.compile("\\s*raw\\s*\\[\\$[0-9A-F]+\\.\\.\\$[0-9A-F]+]\\s*at \\$[0-9A-F]+");
    private static final Pattern VAR = Pattern.compile("\\s*spec var\\s+\\w+\\.\\$\\w+.*");
    private static final Pattern VAR_PREFIXED = Pattern.compile("\\s*@\\w+.*");
    private static final Pattern TYPE = Pattern.compile("\\s*\\w+\\.\\w+\\s*=.*");
    private static final Pattern COMMENTED_TYPE = Pattern.compile("\\s*\\{type}\\s*");
    private static final Pattern ROUTINE = Pattern.compile("(\\s*)(procedure|function|operator)(\\s+)(@)(\\w+)");
    private static final Pattern INLINE_TYPE = Pattern.compile("\\s*:\\d+\\s+=\\s+.*");
    private static final File NULL_FILE = new File("");
    private static final Pattern WARNING_WITH_UNITHEAD = Pattern.compile("(?i)Warning at 0x[A-F0-9]+.*unit\\s+.+;$");

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile file) {
        assert file.getFileType() == DCUFileType.INSTANCE;

        final Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return "";
        final Project project = projects[0];

        return decompileText(project, file);
    }

    static String decompileText(Project project, VirtualFile file) {
        Module module = ModuleUtil.getModuleForLibraryFile(project, file);
        if (null == module) {
            return PascalBundle.message("decompile.no.module", file.getPath());
        }
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (null == sdk) {
            return PascalBundle.message("decompile.wrong.sdk");
        }
        if ((sdk.getHomePath() == null) || !(sdk.getSdkType() instanceof DelphiSdkType)) {
            return PascalBundle.message("decompile.wrong.sdktype.delphi");
        }

        File decompilerCommand = BasePascalSdkType.getDecompilerCommand(sdk, NULL_FILE);
        String result = "";
        try {
            if (!decompilerCommand.isFile() || !decompilerCommand.canExecute()) {
                return PascalBundle.message("decompile.wrong.delphi", decompilerCommand.getCanonicalPath());
            }
            List<String> paths = collectUnitPaths(sdk);
            String[] args = getArgs(BasePascalSdkType.getDecompilerArgs(sdk), file.getPath(), "-U" + Joiner.on(';').join(paths), "-I", "-SI", "-");
            result = SysUtils.runAndGetStdOut(sdk.getHomePath(), decompilerCommand.getCanonicalPath(), args);
            if (result != null) {
                return handleText(result).replace("\r", "");
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

    private static String[] getArgs(String[] argsArray, String...args) {
        String[] res = new String[args.length + argsArray.length];
        int i = 0;
        for (String arg : args) {
            res[i++] = arg;
        }
        for (String arg : argsArray) {
            res[i++] = arg;
        }
        return res;
    }

    private static List<String> collectUnitPaths(Sdk sdk) {
        VirtualFile[] sdkFiles = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);
        Set<File> paths = com.siberika.idea.pascal.jps.util.FileUtil.retrievePaths(sdkFiles);
        List<String> result = new ArrayList<String>(paths.size());
        for (File path : paths) {
            result.add(path.getAbsolutePath());
        }
        return result;
    }

    private static String handleText(@NotNull String result) {
        String[] lines = result.split("\n");
        boolean unitDone = false;
        boolean inConst = false;
        StringBuffer res = new StringBuffer();
        for (String line : lines) {
            if (isConstant(line)) {                                // Comment out all non-compilable constant declarations
                if (!inConst) {
                    res.append("    default;\n");                  // insert const value
                    inConst = true;
                }
                res.append("// ");
            } else {
                inConst = false;
            }
            if (shouldCommentOut(line)) {                          // Comment out all decompiler warnings
                if (WARNING_WITH_UNITHEAD.matcher(line).matches()) {
                    line = line.replaceAll("(?i)unit\\s+", "\nunit ");
                    unitDone = true;
                }
                res.append("// ");
            } else if (!unitDone) {                                // Comment out all lines before unit declaration
                if (line.startsWith("unit")) {
                    unitDone = true;
                } else {
                    res.append("// ");
                }
            }
            if (isType(line)) {
                res.append("__").append(line.trim());
            } else if (COMMENTED_TYPE.matcher(line).matches()) {
                res.append("  type\n    ");
            } else if (INLINE_TYPE.matcher(line).matches()) {
                res.append(line.replaceFirst(":", "_"));
            } else if (VAR_PREFIXED.matcher(line).matches()) {
                res.append(line.replaceFirst("@", "_"));
            } else {
                Matcher m = ROUTINE.matcher(line);
                if (m.find()) {
                    m.appendReplacement(res, "$1$2$3$5");
                    m.appendTail(res).append("\n");
                } else if (!line.startsWith("procedure Finalization")) {
                    res.append(line).append("\n");
                }
            }
        }
        res.append("implementation\n  {compiled code}\nend.\n");
        return res.toString();
    }

    private static boolean shouldCommentOut(String line) {
        return isWarning(line) || isVar(line);
    }

    private static boolean isConstant(String line) {
        return CONSTANT1.matcher(line).matches() || CONSTANT2.matcher(line).matches();
    }

    private static boolean isWarning(String line) {
        return WARNING1.matcher(line).matches() || WARNING2.matcher(line).matches();
    }

    private static boolean isType(String line) {
        return TYPE.matcher(line).matches();
    }

    private static boolean isVar(String line) {
        return VAR.matcher(line).matches();
    }

}
