package com.siberika.idea.pascal.util;

import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;

/**
 * Author: George Bakhtadze
 * Date: 1/6/13
 */
public class FileUtil {
    public static String getFileDir(final String fileName) {
        int lastSepPos = fileName.lastIndexOf(File.separator);
        return lastSepPos >= 0 ? fileName.substring(0, lastSepPos) : "";

    }

    public static File[] listDirs(File baseDir) {
        return baseDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
    }

    @NotNull
    public static File getExecutable(File path, String command) {
        return new File(path, SystemInfo.isWindows ? command + ".exe" : command);
    }

    public static boolean exists(String filePath) {
        return new File(filePath).exists();
    }
}
