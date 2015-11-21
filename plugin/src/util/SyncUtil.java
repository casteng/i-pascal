package com.siberika.idea.pascal.util;

import com.intellij.openapi.diagnostic.Logger;

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
}
