package com.siberika.idea.pascal.lang.psi;

/**
 * Author: George Bakhtadze
 * Date: 01/03/2015
 */
public class PasInvalidScopeException extends RuntimeException {
    private final PasEntityScope scope;
    public PasInvalidScopeException(PasEntityScope scope) {
        this.scope = scope;
    }
}
