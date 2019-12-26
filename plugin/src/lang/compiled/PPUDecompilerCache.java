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
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.jps.util.SysUtils;
import com.siberika.idea.pascal.module.ModuleService;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2013
 */
public class PPUDecompilerCache implements PascalCachingUnitDecompiler {
    private static final Logger LOG = Logger.getInstance(PPUDecompilerCache.class);

    private static final String PPUDUMP_OPTIONS_COMMON = "-Vhisd";
    private static final String PPUDUMP_OPTIONS_FORMAT = "-Fx";
    private static final String PPUDUMP_OPTIONS_VERSION = "-V";
    private static final String PPUDUMP_VERSION_MIN = "2.7.1";

    private final Module module;
    private final Sdk sdk;
    private final LoadingCache<String, PPUDumpParser.Section> cache;

    PPUDecompilerCache(@NotNull Module module, @Nullable Sdk sdk) {
        this.module = module;
        this.sdk = sdk != null ? sdk : ModuleRootManager.getInstance(module).getSdk();
        this.cache = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.HOURS).build(new Loader());
    }

    @Override
    public String getSource(@NotNull VirtualFile virtualFile) {
        final PPUDumpParser.Section contents = getContents(virtualFile.getNameWithoutExtension(), virtualFile);
        return contents != null ? contents.getResult() : "";
    }

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
                    LOG.info(String.format("Error: Exception while decompiling unit '%s': %s", unitName, e.getMessage()), e);
                    return new PPUDumpParser.Section(PascalBundle.message("decompile.unknown.exception", e.getMessage()));
                }
            }
        }
        return new PPUDumpParser.Section(PascalBundle.message("decompile.unit.not.found", unitName));
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
        ModuleService.getInstance(module).ensureCache(module, false);
        VirtualFile file = module.getComponent(ModuleService.class).getFileByUnitName(key);
        if (file != null) {
            return SysUtils.runAndGetStdOut(sdk.getHomePath(), ppuDump.getCanonicalPath(), SysUtils.LONG_TIMEOUT, PPUDUMP_OPTIONS_COMMON, PPUDUMP_OPTIONS_FORMAT, file.getPath());
        } else {
            throw new PascalRTException(PascalBundle.message("decompile.file.notfound", key));
        }
    }

    File retrievePpuDump(String key) throws IOException {
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
            res = SysUtils.runAndGetStdOut(ppuDump.getParent(), ppuDump.getCanonicalPath(), SysUtils.SHORT_TIMEOUT, PPUDUMP_OPTIONS_COMMON, PPUDUMP_OPTIONS_VERSION);
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

    VirtualFile retrieveFile(Module module, String unitName) {
        return module.getComponent(ModuleService.class).getFileByUnitName(unitName);
    }

    // TODO: use full file name as key to avoid using wrong file in case of .ppu with the same name in different modules
    private String getKey(String unitName) {
        return FileUtil.getNameWithoutExtension(unitName);
    }

}
