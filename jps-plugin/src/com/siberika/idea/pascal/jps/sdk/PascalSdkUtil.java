package com.siberika.idea.pascal.jps.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.siberika.idea.pascal.jps.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 09/05/2014
 */
public class PascalSdkUtil {

    public static final Logger LOG = Logger.getInstance(PascalSdkUtil.class.getName());

    public static final String FPC_PARAMS_VERSION_GET = "-iV";
    public static final String FPC_PARAMS_TARGET_GET = "-iTPTO";
    public static final String DELPHI_PARAMS_VERSION_GET = "--version";

    public static final Pattern FPC_VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+");
    public static final String[] DEFAULT_BIN_UNIX = {"/usr/bin", "/usr/local/bin"};

    public static String target;

    @NotNull
    public static File getFPCExecutable(@NotNull final String sdkHome) {
        LOG.info("Getting executable: " + sdkHome);
        return getFPCUtilExecutable(sdkHome, "bin", "fpc");
    }

    @NotNull
    public static File getPPUDumpExecutable(@NotNull final String sdkHome) {
        LOG.info("Getting ppudump: " + sdkHome);
        return getFPCUtilExecutable(sdkHome, "bin", "ppudump");
    }

    public static File getDCC32Executable(@NotNull final String sdkHome) {
        File binDir = new File(sdkHome, "bin");
        return getExecutable(binDir.getAbsolutePath(), "dcc32");
    }

    //TODO: take target directory from compiler target
    /*
      /$dir/
      /$ver/$dir/
      /$ver
      => $default
      => $bindir/[$target]/$exe
     */
    @NotNull
    static File getFPCUtilExecutable(@NotNull final String sdkHome, @NotNull final String dir, @NotNull final String exe) {
        LOG.info("Getting util executable: " + sdkHome + ", dir: " + dir + ", file: " + exe);
        File binDir = new File(sdkHome, dir);
        File sdkHomeDir = new File(sdkHome);
        if (!binDir.exists() && sdkHomeDir.isDirectory()) {
            LOG.info(binDir.getAbsolutePath() + " not found, trying $SDKHome/$Version/bin/...");
            String currentVersion = getFPCVersionDir(sdkHomeDir);
            if (currentVersion != null) {
                binDir = new File(new File(sdkHome, currentVersion), dir);
                if (!binDir.exists()) {
                    LOG.info(binDir.getAbsolutePath() + " not found, trying without $SDKHome/$Version/...");
                    binDir = new File(sdkHome, currentVersion);
                }
            } else {
                binDir = sdkHomeDir;
            }
        }
        if (binDir.exists()) {
            LOG.info("Binary directory found at " + binDir.getAbsolutePath());
            for (File targetDir : FileUtil.listDirs(binDir)) {
                File executable = getExecutable(targetDir.getAbsolutePath(), exe);
                if (executable.canExecute()) {
                    target = targetDir.getName();
                    LOG.info("Found target " + target);
                    return executable;
                }
            }
            File executable = getExecutable(binDir.getAbsolutePath(), exe);
            if (executable.exists() && executable.canExecute()) {
                return executable;
            }
        }
        // Default directory where fpc and ppudump executables are located
        LOG.info(binDir.getAbsolutePath() + " not found, trying default executable path...");
        for (String defaultBinDir : DEFAULT_BIN_UNIX) {
            File executable = new File(defaultBinDir, exe);
            if (executable.exists() && executable.canExecute()) {
                return executable;
            }
        }
        LOG.info("Binary directory not found");
        throw new RuntimeException("SDK not found");
    }

    @Nullable
    private static String getFPCVersionDir(File sdkHomeDir) {
        String currentVersion = null;
        for (File versionDir : FileUtil.listDirs(sdkHomeDir)) {
            if (isFPCVersion(versionDir.getName()) &&
                    ((currentVersion == null) || isVersionLessOrEqual(currentVersion, versionDir.getName()))) {
                currentVersion = versionDir.getName();
            }
        }
        return currentVersion;
    }

    private static boolean isFPCVersion(String name) {
        return FPC_VERSION_PATTERN.matcher(name).matches();
    }

    private static boolean isVersionLessOrEqual(String version1, String version2) {
        return version1.compareTo(version2) <= 0;
    }

    @NotNull
    public static File getByteCodeCompilerExecutable(@NotNull final String sdkHome) {
        return getExecutable(new File(sdkHome, "bin").getAbsolutePath(), "ppcjvm");
    }

    @NotNull
    private static File getExecutable(@NotNull final String path, @NotNull final String command) {
        return new File(path, SystemInfo.isWindows ? command + ".exe" : command);
    }

}
