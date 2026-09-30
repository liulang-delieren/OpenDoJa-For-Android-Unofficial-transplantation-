package com.nttdocomo.lang;

/**
 * Runtime exception thrown when an operation or method is not supported.
 */
public class UnsupportedOperationException extends RuntimeException {
    /**
     * Creates an exception object without a detail message.
     */
    public UnsupportedOperationException() {
    }

    /**
     * Creates an exception object with a detail message.
     *
     * @param message the detail message
     */
    public UnsupportedOperationException(String message) {
        super(message);
    }
}
