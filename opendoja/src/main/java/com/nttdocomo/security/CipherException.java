package com.nttdocomo.security;

/**
 * Defines exceptions related to the encryption function.
 */
public class CipherException extends Exception {
    /** Indicates that the status is undefined (=0). */
    public static final int UNDEFINED = 0;

    /** Indicates that the data is not padded appropriately (=1). */
    public static final int BAD_PADDING = 1;

    private final int status;

    /**
     * Creates an exception object without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public CipherException() {
        this(UNDEFINED);
    }

    /**
     * Creates an exception object with a detail message.
     * The status is set to {@link #UNDEFINED}.
     *
     * @param msg the detail message
     */
    public CipherException(String msg) {
        this(UNDEFINED, msg);
    }

    /**
     * Creates an exception object with a status value.
     *
     * @param status the status value to set
     */
    public CipherException(int status) {
        this(status, null);
    }

    /**
     * Creates an exception object with a status value and a detail message.
     *
     * @param status the status value to set
     * @param msg the detail message
     */
    public CipherException(int status, String msg) {
        super(msg);
        this.status = status;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }
}
