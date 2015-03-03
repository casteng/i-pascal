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
    public static final Pattern VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+");
    public static final String DEFAULT_BIN_UNIX = "/usr/bin";

    public static String target;

    @NotNull
    public static File getCompilerExecutable(@NotNull final String sdkHome) {
        LOG.info("Getting executable: " + sdkHome);
        return getUtilExecutable(sdkHome, "bin", "fpc");
    }

    @NotNull
    public static File getPPUDumpExecutable(@NotNull final String sdkHome) {
        LOG.info("Getting ppudump: " + sdkHome);
        return getUtilExecutable(sdkHome, "bin", "ppudump");
    }

    //TODO: take target directory from compiler target
    @NotNull
    static File getUtilExecutable(@NotNull final String sdkHome, @NotNull final String dir, @NotNull final String exe) {
        LOG.info("Getting util executable: " + sdkHome + ", dir: " + dir + ", file: " + exe);
        File binDir = new File(sdkHome, dir);
        File sdkHomeDir = new File(sdkHome);
        if (!binDir.exists() && sdkHomeDir.isDirectory()) {
            LOG.info(binDir.getAbsolutePath() + " not found, trying $SDKHome/$Version/bin/...");
            String currentVersion = getVersionDir(sdkHomeDir);
            if (currentVersion != null) {
                binDir = new File(new File(sdkHome, currentVersion), dir);
                if (!binDir.exists()) {
                    LOG.info(binDir.getAbsolutePath() + " not found, trying without $SDKHome/$Version/...");
                    binDir = new File(sdkHome, currentVersion);
                }
            }
        }
        if (!binDir.exists()) {                  // Default directory where fpc and ppudump executables are located
            LOG.info(binDir.getAbsolutePath() + " not found, trying default executable path...");
            binDir = "bin".equals(dir) ? getDefaultBinDir(exe) : null;
            if ((null != binDir) && binDir.exists()) {
                return binDir;
            } else {
                LOG.info("Binary directory not found");
                throw new RuntimeException("SDK not found");
            }
        }
        LOG.info("Binary directory found at " + binDir.getAbsolutePath());
        for (File targetDir : FileUtil.listDirs(binDir)) {
            File executable = getExecutable(targetDir.getAbsolutePath(), exe);
            if (executable.canExecute()) {
                target = targetDir.getName();
                LOG.info("Found target " + target);
                return executable;
            }
        }
        return binDir;
    }

    private static File getDefaultBinDir(String exe) {
        return new File(DEFAULT_BIN_UNIX, exe);
    }

    @Nullable
    private static String getVersionDir(File sdkHomeDir) {
        String currentVersion = null;
        for (File versionDir : FileUtil.listDirs(sdkHomeDir)) {
            if (isVersion(versionDir.getName()) &&
                    ((currentVersion == null) || isVersionLessOrEqual(currentVersion, versionDir.getName()))) {
                currentVersion = versionDir.getName();
            }
        }
        return currentVersion;
    }

    private static boolean isVersion(String name) {
        return VERSION_PATTERN.matcher(name).matches();
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
