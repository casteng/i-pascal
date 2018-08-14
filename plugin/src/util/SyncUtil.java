package com.siberika.idea.pascal.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
            while (!lock.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                ProgressManager.checkCanceled();
            }
//            lock.lockInterruptibly();
            return true;
        } catch (InterruptedException e) {
            LOG.warn("Interrupted thread", e);
            return false;
        }
    }

    public static void doWithLock(ReentrantLock nameLock, Runnable runnable) {
        if (SyncUtil.lockOrCancel(nameLock)) {
            try {
                runnable.run();
            } finally {
                nameLock.unlock();
            }
        }
    }
}
