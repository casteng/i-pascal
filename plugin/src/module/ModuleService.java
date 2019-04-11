package com.siberika.idea.pascal.module;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleService implements ModuleComponent {

    private static final Logger LOG = Logger.getInstance(ModuleService.class);

    private static final long CACHE_TTL_MS = 30000;

    private static final ModuleService INSTANCE_DEFAULT = new ModuleService();

    private final Map<Object, SmartPsiElementPointer> cache = new ConcurrentHashMap<>();
    private long lastClearTime = System.nanoTime();

    public static ModuleService getInstance(@Nullable Module module) {
        if (module != null) {
            return module.getComponent(ModuleService.class);
        } else {
            return INSTANCE_DEFAULT;
        }
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
}
