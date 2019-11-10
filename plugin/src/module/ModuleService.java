package com.siberika.idea.pascal.module;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ModuleService implements ModuleComponent {

    private static final Logger LOG = Logger.getInstance(ModuleService.class);

    private static final ModuleService INSTANCE_DEFAULT = new ModuleService();

    private static final long CACHE_TTL_MS = 30000;

    private final Map<Object, SmartPsiElementPointer> cache = new ConcurrentHashMap<>();
    private long lastClearTime = System.nanoTime();
    private final ReentrantLock cacheNameFileLock = new ReentrantLock();
    private final Map<String, VirtualFile> cacheNameFileMap = new ConcurrentHashMap<>();
    private long lastClearTimeNameFile = 0;

    public static ModuleService getInstance(@Nullable Module module) {
        if (module != null) {
            return module.getComponent(ModuleService.class);
        } else {
            return INSTANCE_DEFAULT;
        }
    }

    public static void ensureNameFileCache(VirtualFile file, Project project, boolean checkTTL) {
        Module module = ModuleUtil.findModuleForFile(file, project);
        ModuleService.getInstance(module).ensureCache(module, checkTTL);
    }

    public <K, V extends PsiElement> V calcWithCache(K key, Callable<V> callable) {
        V result = null;
        if (ensureCacheFresh()) {
            SmartPsiElementPointer resultPtr = cache.get(key);
            if (resultPtr != null) {
                result = (V) resultPtr.getElement();
                if (!PsiUtil.isElementUsable(result)) {
                    LOG.info("--- Cached value became invalid");
                    result = null;
                }
            }
        }
        if (null == result) {
            try {
                result = callable.call();
                if (result != null) {
                    cache.put(key, PsiUtil.createSmartPointer(result));
                }
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Exception e) {
                LOG.error("Error calculating value", e);
            }
        }
        return result;
    }

    private boolean ensureCacheFresh() {
        long currentTime = System.nanoTime();
        if (currentTime - lastClearTime > CACHE_TTL_MS * 1000000) {
            cache.clear();
            lastClearTime = currentTime;
            return false;
        } else {
            return true;
        }
    }

    public VirtualFile getFileByUnitName(String unitName) {
        return SyncUtil.doWithLock(cacheNameFileLock, () -> cacheNameFileMap.get(unitName));
    }

    public void ensureCache(@Nullable Module module, boolean checkTTL) {
        if (null == module) {
            return;
        }
        long currentTime = System.nanoTime();
        if ((lastClearTimeNameFile == 0) || (checkTTL && ((currentTime - lastClearTimeNameFile) > (CACHE_TTL_MS * 1000000)))) {
            fillCache(module);
        }
    }

    private void fillCache(@NotNull Module module) {
        SyncUtil.doWithLock(cacheNameFileLock, () -> {
            cacheNameFileMap.clear();
            lastClearTimeNameFile = System.nanoTime();;
            ApplicationManager.getApplication().runReadAction(() -> {
                FileTypeIndex.processFiles(PPUFileType.INSTANCE, new Processor<VirtualFile>() {
                    @Override
                    public boolean process(VirtualFile file) {
                        cacheNameFileMap.put(file.getNameWithoutExtension(), file);
                        return true;
                    }
                }, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
            });
        });
    }

}
