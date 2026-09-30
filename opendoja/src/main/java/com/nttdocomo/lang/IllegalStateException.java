package com.nttdocomo.lang;

/**
 * Runtime exception thrown when an operation or method is called while the
 * object is in a state in which that operation is not allowed.
 *
 * <p>Introduced in DoJa-2.0.</p>
 */
public class IllegalStateException extends RuntimeException {
    /**
     * Creates an exception object without a detail message.
     */
    public IllegalStateException() {
    }

    /**
     * Creates an exception object with a detail message.
     *
     * @param message the detail message
     */
    public IllegalStateException(String message) {
        super(message);
    }
}
