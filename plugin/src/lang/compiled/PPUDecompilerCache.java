package com.siberika.idea.pascal.lang.compiled;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.StrUtil;
import com.siberika.idea.pascal.util.SysUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2013
 */
public class PPUDecompilerCache {
    private static final Logger LOG = Logger.getInstance(PPUDecompilerCache.class);

    private static final String PPUDUMP_OPTIONS_COMMON = "-Vhisd";
    private static final String PPUDUMP_OPTIONS_FORMAT = "-Fx";
    private static final String PPUDUMP_OPTIONS_VERSION = "-V";
    private static final String PPUDUMP_VERSION_MIN = "2.7.1";

    private final Module module;
    private final LoadingCache<String, PPUDumpParser.Section> cache;

    public PPUDecompilerCache(@NotNull Module module) {
        this.module = module;
        cache = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.HOURS).build(new Loader());
    }

    public static String decompile(Module module, String filename, @Nullable VirtualFile file) {
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (null == sdk) { return PascalBundle.message("decompile.wrong.sdk"); }
        PPUDecompilerCache decompilerCache;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (sdk) {
            decompilerCache = (PPUDecompilerCache) BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.Keys.DECOMPILER_CACHE.getKey());
            if ((null == decompilerCache) || (decompilerCache.module != module)) {
                decompilerCache = new PPUDecompilerCache(module);
                BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.Keys.DECOMPILER_CACHE.getKey(), decompilerCache);
            }
        }
        String unitName = FileUtil.getNameWithoutExtension(com.siberika.idea.pascal.jps.util.FileUtil.getFilename(filename));
        PPUDumpParser.Section stub = decompilerCache.getContents(unitName, file);
        return stub != null ? stub.getResult() : "";
    }

    private class Loader extends CacheLoader<String, PPUDumpParser.Section> {
        @Override
        public PPUDumpParser.Section load(@NotNull String key) {
            File ppuDump = null;
            String xml = "";
            try {
                ppuDump = retrievePpuDump(key);
                xml = retrieveXml(key, ppuDump);
                if (xml != null) {
                    return PPUDumpParser.parse(xml, PPUDecompilerCache.this);
                } else {
                    return new PPUDumpParser.Section(PascalBundle.message("decompile.empty.result"));
                }
            } catch (PascalRTException e) {
                LOG.info("Exception: " + e.getMessage(), e);
                return new PPUDumpParser.Section(e.getMessage());
            } catch (IOException e) {
                LOG.info("I/O error: " + e.getMessage(), e);
                return new PPUDumpParser.Section(PascalBundle.message("decompile.io.error"));
            } catch (ParseException e) {
                LOG.info("Parse error: " + e.getMessage(), e);
                String ver = getPPUDumpVersion(ppuDump);
                if (ver.compareTo(PPUDUMP_VERSION_MIN) < 0) {
                    return new PPUDumpParser.Section(PascalBundle.message("decompile.version.error", ver, PPUDUMP_VERSION_MIN));
                } else {
                    return new PPUDumpParser.Section(PascalBundle.message("decompile.parse.error", xml));
                }
            } catch (PascalException e1) {
                return new PPUDumpParser.Section(e1.getMessage());
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Exception e) {
                LOG.info("Unknown error: " + e.getMessage(), e);
                return new PPUDumpParser.Section(PascalBundle.message("decompile.unknown.error", StrUtil.limit(xml, 2048)));
            }
        }
    }

    String retrieveXml(String key, File ppuDump) throws IOException, PascalException {
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        Collection<VirtualFile> files = ModuleUtil.getCompiledByNameNoCase(module, key, PPUFileType.INSTANCE);
        if (files.isEmpty()) {
            throw new PascalRTException(PascalBundle.message("decompile.file.notfound", key));
        }
        return SysUtils.runAndGetStdOut(sdk.getHomePath(), ppuDump.getCanonicalPath(), PPUDUMP_OPTIONS_COMMON, PPUDUMP_OPTIONS_FORMAT, files.iterator().next().getPath());
    }

    File retrievePpuDump(String key) throws IOException {
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (null == sdk) { throw new PascalRTException(PascalBundle.message("decompile.wrong.sdk")); }
        if ((sdk.getHomePath() == null) || !(sdk.getSdkType() instanceof FPCSdkType)) {
            throw new PascalRTException(PascalBundle.message("decompile.wrong.sdktype"));
        }
        File ppuDump = BasePascalSdkType.getDecompilerCommand(sdk, PascalSdkUtil.getPPUDumpExecutable(sdk.getHomePath() != null ? sdk.getHomePath() : ""));
        if (!ppuDump.isFile() || !ppuDump.canExecute()) {
            throw new PascalRTException(PascalBundle.message("decompile.wrong.ppudump", ppuDump.getCanonicalPath()));
        }
        return ppuDump;
    }

    private static String getPPUDumpVersion(File ppuDump) {
        String res = "";
        try {
            res = SysUtils.runAndGetStdOut(ppuDump.getParent(), ppuDump.getCanonicalPath(), PPUDUMP_OPTIONS_COMMON, PPUDUMP_OPTIONS_VERSION);
            if (res != null) {
                int i1 = res.indexOf("Version");
                int i2 = res.indexOf("\n");
                if ((i1 < res.length()) && (i2 > i1)) {
                    res = res.substring(i1 + 8, i2);
                }
            }
        } catch (Exception e) {
            LOG.info("Error: " + e.getMessage(), e);
        }
        return res;
    }

    /**
     * Retrieves decompiled contents from cache.
     * Seems that compiled modules can't be found during PSI reparse. For that case virtualFile parameter is used.
     * @param unitName       compiled unit name
     * @param virtualFile    if this is not null it should be used instead of search by name
     * @return               decompiled data
     */
    PPUDumpParser.Section getContents(@NotNull String unitName, @Nullable VirtualFile virtualFile) {
        VirtualFile file = virtualFile;
        if (null == file) {
            file = retrieveFile(module, unitName);
        }
        if (file != null) {
            try {
                String key = getKey(file.getName());
                PPUDumpParser.Section section = cache.getIfPresent(key);
                boolean needReparsePsi = null == section;
                section = cache.get(key);
                if (section.isError()) {
                    LOG.info("ERROR: Invalidating ppu cache for key: " + key);
                    cache.invalidate(key);
                } else if (needReparsePsi) {
                    DocUtil.reparsePsi(module.getProject(), file);
                }
                return section;
            } catch (Exception e) {
                if (e.getCause() instanceof ProcessCanceledException) {
                    throw (ProcessCanceledException) e.getCause();
                } else {
                    LOG.info(String.format("Error: Exception while decompiling unit %s: %s", unitName, e.getMessage()), e);
                }
            }
        }
        return new PPUDumpParser.Section(PascalBundle.message("decompile.unit.not.found", unitName));
    }

    VirtualFile retrieveFile(Module module, String unitName) {
        Collection<VirtualFile> files = ModuleUtil.getCompiledByNameNoCase(module, unitName, PPUFileType.INSTANCE);
        if (!files.isEmpty()) {
            return files.iterator().next();
        }
        return null;
    }

    private String getKey(String unitName) {
        return FileUtil.getNameWithoutExtension(unitName);
    }

}
