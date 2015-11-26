package com.siberika.idea.pascal.util;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2015
 */
public interface Filter<T> {
    boolean allow(T value);
}
