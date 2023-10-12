package com.siberika.idea.pascal.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Author: George Bakhtadze
 * Date: 21/11/2015
 */
public class SyncUtil {

    protected static final Logger LOG = Logger.getInstance(SyncUtil.class.getName());
    public static final int LOCK_TIMEOUT_MS = 50;

    public static boolean tryLockQuiet(Lock lock, int timeoutMs) {
        try {
            return lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted thread", e);
            return false;
        }
    }

    public static boolean lockOrCancel(Lock lock) {
        try {
/*            while (!lock.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                ProgressManager.checkCanceled();
            }*/
            return lock.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted thread", e);
            return false;
        }
    }

    public static void doWithLock(Lock lock, Runnable runnable) {
        if (SyncUtil.lockOrCancel(lock)) {
            try {
                runnable.run();
            } finally {
                lock.unlock();
            }
        }
    }

    public static <T> T doWithLock(Lock lock, Callable<T> runnable) {
        if (SyncUtil.lockOrCancel(lock)) {
            try {
                return runnable.call();
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Exception e) {
                LOG.warn("ERROR: doWithLock: ", e);
                return null;
            } finally {
                lock.unlock();
            }
        } else {
            throw new ProcessCanceledException();
        }
    }
}
