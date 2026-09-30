package com.nttdocomo.device;

/**
 * Defines Bluetooth exceptions.
 * This exception is thrown to indicate a Bluetooth runtime exception.
 */
public class BluetoothException extends RuntimeException {
    /** Indicates that the status is undefined (=0). */
    public static final int UNDEFINED = 0;

    /** Indicates that Bluetooth cannot be used because the terminal is in self mode (=1). */
    public static final int SELF_MODE = 1;

    /** Indicates that a timeout occurred (=2). */
    public static final int TIMEOUT = 2;

    /** Indicates that processing was interrupted by a user operation (=3). */
    public static final int USER_ABORT = 3;

    /** Indicates that the link key is invalid (=4). */
    public static final int LINKKEY_ERROR = 4;

    /** Indicates that the profile is unsupported by the external device (=5). */
    public static final int UNKNOWN_PROFILE = 5;

    private final int status;

    /**
     * Creates a Bluetooth exception object without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public BluetoothException() {
        this(UNDEFINED);
    }

    /**
     * Creates a Bluetooth exception object with a status value indicating the cause.
     *
     * @param status the status value to set
     */
    public BluetoothException(int status) {
        this(status, null);
    }

    /**
     * Creates a Bluetooth exception object with a status value indicating the cause and a detail message.
     *
     * @param status the status value to set
     * @param message the detail message
     */
    public BluetoothException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Gets the status value indicating the cause of the exception.
     *
     * @return the status value
     */
    public int getStatus() {
        return status;
    }
}
