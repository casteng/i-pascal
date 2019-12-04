package com.siberika.idea.pascal.jps.sdk;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2015
 */
public enum PascalCompilerFamily {
    FPC, DELPHI;

    public static PascalCompilerFamily of(String name) {
        for (PascalCompilerFamily compilerFamily : PascalCompilerFamily.values()) {
            if (compilerFamily.name().equals(name)) {
                return compilerFamily;
            }
        }
        return null;
    }
}
