package com.siberika.idea.pascal.util;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;

/**
 * Author: George Bakhtadze
 * Date: 1/6/13
 */
public class FileUtil {
    public static String getPath(@NotNull final String fileName) {
        int lastSepPos = fileName.lastIndexOf(File.separator);
        return lastSepPos >= 0 ? fileName.substring(0, lastSepPos) : "";
    }

    /**
     * Returns file name without path
     * @param fullPath - file name with full path
     * @return file name without full path
     */
    public static String getFilename(@NotNull final String fullPath) {
        return fullPath.substring(fullPath.lastIndexOf(File.separator)+1);
    }

    public static File[] listDirs(@NotNull File baseDir) {
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

    public static String getExtension(@NotNull String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) return null;
        return fileName.substring(index + 1);
    }

    public static VirtualFile getVirtualFile(String path) {
        return LocalFileSystem.getInstance().findFileByPath(path.replace(File.separatorChar, '/'));
    }
}
