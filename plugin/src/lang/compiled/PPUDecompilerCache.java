package com.siberika.idea.pascal.lang.compiled;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.SysUtils;
import org.jetbrains.annotations.NotNull;

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

    public static final String PPUDUMP_OPTIONS_COMMON = "-Vhisd";
    public static final String PPUDUMP_OPTIONS_FORMAT = "-Fx";
    public static final String PPUDUMP_OPTIONS_VERSION = "-V";
    public static final String PPUDUMP_VERSION_MIN = "2.7.1";

    private final Module module;
    private final PPUDecompilerCache self;
    private final LoadingCache<String, PPUDumpParser.Section> cache;

    public PPUDecompilerCache(@NotNull Module module) {
        this.module = module;
        self = this;
        cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build(new Loader());//===***
    }

    synchronized public static String decompile(Module module, String filename) {
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (null == sdk) { return PascalBundle.message("decompile.wrong.sdk"); }
        PPUDecompilerCache decompilerCache = (PPUDecompilerCache) BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.DATA_KEY_DECOMPILER_CACHE);
        if (null == decompilerCache) {
            decompilerCache = new PPUDecompilerCache(module);
            BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.DATA_KEY_DECOMPILER_CACHE, decompilerCache);
        }
        String unitName = FileUtil.getNameWithoutExtension(com.siberika.idea.pascal.jps.util.FileUtil.getFilename(filename));
        return decompilerCache.getContents(unitName).getResult();                 // TODO: check for null
    }

    private class Loader extends CacheLoader<String, PPUDumpParser.Section> {
        @Override
        public PPUDumpParser.Section load(String key) throws Exception {
            Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
            if (null == sdk) { return new PPUDumpParser.Section(PascalBundle.message("decompile.wrong.sdk")); }
            if ((sdk.getHomePath() == null) || !(sdk.getSdkType() instanceof FPCSdkType)) {
                return new PPUDumpParser.Section(PascalBundle.message("decompile.wrong.sdktype"));
            }
            Collection<VirtualFile> files = ModuleUtil.getAllCompiledModuleFilesByName(module, key);
            if (files.isEmpty()) {
                return new PPUDumpParser.Section(PascalBundle.message("decompile.ppu.notfound", key));
            }
            File ppuDump = PascalSdkUtil.getPPUDumpExecutable(sdk.getHomePath());
            String xml = "";
            try {
                if (!ppuDump.isFile() || !ppuDump.canExecute()) {
                    return new PPUDumpParser.Section(PascalBundle.message("decompile.wrong.ppudump", ppuDump.getCanonicalPath()));
                }
                xml = SysUtils.runAndGetStdOut(sdk.getHomePath(), ppuDump.getCanonicalPath(), PPUDUMP_OPTIONS_COMMON, PPUDUMP_OPTIONS_FORMAT, files.iterator().next().getPath());
                if (xml != null) {
                    return PPUDumpParser.parse(xml, self);
                } else {
                    return new PPUDumpParser.Section(PascalBundle.message("decompile.empty.ppudump.result"));
                }
            } catch (IOException e) {
                LOG.warn(e.getMessage(), e);
                return new PPUDumpParser.Section(PascalBundle.message("decompile.io.error"));
            } catch (ParseException e) {
                LOG.warn(e.getMessage(), e);
                String ver = getPPUDumpVersion(ppuDump);
                if (ver.compareTo(PPUDUMP_VERSION_MIN) < 0) {
                    return new PPUDumpParser.Section(PascalBundle.message("decompile.version.error", ver, PPUDUMP_VERSION_MIN));
                } else {
                    return new PPUDumpParser.Section(PascalBundle.message("decompile.parse.error", xml));
                }
            } catch (PascalException e1) {
                return new PPUDumpParser.Section(e1.getMessage());
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
                return new PPUDumpParser.Section(PascalBundle.message("decompile.unknown.error", xml));
            }
        }
    }

    static String getPPUDumpVersion(File ppuDump) {
        String res = "";
        try {
            res = SysUtils.runAndGetStdOut(ppuDump.getParent(), ppuDump.getCanonicalPath(), PPUDUMP_OPTIONS_COMMON, PPUDUMP_OPTIONS_VERSION);
            if (res != null) {
                int i1 = res.indexOf("Version");
                int i2 = res.indexOf("\n");
                if ((i1 < res.length()) && (i2 > i1)) {
                    res = res.substring(i1+8, i2);
                }
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
        return res;
    }

    PPUDumpParser.Section getContents(String unitName) {
        Collection<VirtualFile> unitFiles = ModuleUtil.getAllCompiledModuleFilesByName(module, unitName);
        if (!unitFiles.isEmpty()) {
            try {
                return cache.get(FileUtil.getNameWithoutExtension(unitFiles.iterator().next().getName()));
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
        return null;
    }

}
