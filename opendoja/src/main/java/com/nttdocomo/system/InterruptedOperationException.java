package com.nttdocomo.system;

/**
 * Indicates that a user-interface operation was terminated for reasons other than the user's intent.
 */
public class InterruptedOperationException extends Exception {
    /**
     * Creates an exception object without a detail message.
     */
    public InterruptedOperationException() {
    }

    /**
     * Creates an exception object with a detail message.
     *
     * @param message the detail message
     */
    public InterruptedOperationException(String message) {
        super(message);
    }
}
