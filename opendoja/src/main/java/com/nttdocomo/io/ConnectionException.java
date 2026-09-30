package com.nttdocomo.io;

import java.io.IOException;

/**
 * Defines communication exceptions.
 * The details of the exception can be obtained with the {@link #getStatus()}
 * method.
 */
public class ConnectionException extends IOException {
    /** Status value indicating an invalid state (=1). */
    public static final int ILLEGAL_STATE = 1;
    /**
     * Status value indicating that resources cannot be secured (=2).
     *
     * @deprecated replaced by {@link #NO_RESOURCES}
     */
    @Deprecated
    public static final int NO_RESOURCE = 2;
    /** Status value indicating that resources cannot be secured (=2). */
    public static final int NO_RESOURCES = 2;
    /**
     * Status value indicating that a resource is busy (=3).
     *
     * @deprecated replaced by {@link #BUSY_RESOURCE}
     */
    @Deprecated
    public static final int RESOURCE_BUSY = 3;
    /** Status value indicating that a resource is busy (=3). */
    public static final int BUSY_RESOURCE = 3;
    /** Status value indicating that the function cannot be used (=4). */
    public static final int NO_USE = 4;
    /** Status value indicating that the terminal is outside the service area (=5). */
    public static final int OUT_OF_SERVICE = 5;
    /** Status value indicating that i-mode is locked (=6). */
    public static final int IMODE_LOCKED = 6;
    /** Status value indicating that processing timed out (=7). */
    public static final int TIMEOUT = 7;
    /** Status value indicating that the user aborted the operation (=8). */
    public static final int USER_ABORT = 8;
    /** Status value indicating that the system aborted the operation (=9). */
    public static final int SYSTEM_ABORT = 9;
    /** Status value indicating that an HTTP error occurred (=10). */
    public static final int HTTP_ERROR = 10;
    /** Status value indicating that the scratchpad size limit was exceeded (=11). */
    public static final int SCRATCHPAD_OVERSIZE = 11;
    /** Status value indicating that an OBEX error occurred (=12). */
    public static final int OBEX_ERROR = 12;
    /** Status value indicating self mode (=13). */
    public static final int SELF_MODE = 13;
    /** Status value indicating an SSL error (=14). */
    public static final int SSL_ERROR = 14;
    /** Status value indicating that the UART connection was disconnected (=15). */
    public static final int UART_DISCONNECTED = 15;
    /** Status value indicating that the cause is undefined (=0). */
    public static final int UNDEFINED = 0;
    /** The first defined status value (=0). */
    public static final int STATUS_FIRST = 0;
    /** The last defined status value (=32). */
    public static final int STATUS_LAST = 32;

    private final int status;

    /**
     * Creates a communication exception object without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public ConnectionException() {
        this(UNDEFINED);
    }

    /**
     * Creates a communication exception object with a status value that
     * indicates the cause of the exception.
     *
     * @param status the integer value that represents the cause of the
     *               exception
     */
    public ConnectionException(int status) {
        this(status, null);
    }

    /**
     * Creates a communication exception object with a status value that
     * indicates the cause of the exception and a detail message.
     *
     * @param status the integer value that represents the cause of the
     *               exception
     * @param message the detail message
     */
    public ConnectionException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Gets the status value that represents the cause of the exception.
     *
     * @return the integer value that represents the status
     */
    public synchronized int getStatus() {
        return status;
    }
}
