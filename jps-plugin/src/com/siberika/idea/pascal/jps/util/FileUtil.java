package com.siberika.idea.pascal.jps.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 1/6/13
 */
public class FileUtil {

    private static final Logger LOG = Logger.getInstance(FileUtil.class);

    public static String getPath(@NotNull final String fileName) {
        String res = new File(fileName).getParent();
        return res != null ? res : "";
    }

    /**
     * Returns file name without path
     * @param fullPath - file name with full path
     * @return file name without full path
     */
    public static String getFilename(@NotNull final String fullPath) {
        return new File(fullPath).getName();
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

    public static Set<File> retrievePaths(List<File> files) {
        Set<File> result = new HashSet<File>();
        if (files != null) {
            for (File file : files) {
                collectFile(result, file);
            }
        }
        return result;
    }

    public static Set<File> retrievePaths(VirtualFile[] files) {
        Set<File> result = new HashSet<File>();
        if (files != null) {
            for (VirtualFile virtualFile : files) {
                File file = virtualFile.getCanonicalPath() != null ? new File(virtualFile.getCanonicalPath()) : null;
                collectFile(result, file);
            }
        }
        return result;
    }

    private static void collectFile(Set<File> result, File file) {
        if ((file != null) && file.exists()) {
            if (file.isDirectory()) {
                result.add(file);
            } else {
                result.add(file.getParentFile());
            }
        }
    }

    public static String getCanonicalPath(File file) {
        try {
            return file != null ? file.getCanonicalPath() : null;
        } catch (IOException e) {
            LOG.error("Error getting canonical path", e);
            return null;
        }
    }
}
