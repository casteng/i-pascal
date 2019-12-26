package com.siberika.idea.pascal;

/**
 * Author: George Bakhtadze
 * Date: 29/01/2015
 */
public class PascalException extends Exception {
    public PascalException() {
    }

    public PascalException(String message) {
        super(message);
    }

    public PascalException(String message, Throwable cause) {
        super(message, cause);
    }
}
