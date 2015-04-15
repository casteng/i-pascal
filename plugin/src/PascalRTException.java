package com.siberika.idea.pascal;

/**
 * Author: George Bakhtadze
 * Date: 29/01/2015
 */
public class PascalRTException extends RuntimeException {
    public PascalRTException() {
    }

    public PascalRTException(String message) {
        super(message);
    }

    public PascalRTException(String message, Throwable cause) {
        super(message, cause);
    }
}
