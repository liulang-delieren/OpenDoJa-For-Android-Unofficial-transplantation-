package com.nttdocomo.device.location;

/**
 * Defines location exceptions.
 */
public class LocationException extends Exception {
    /** Indicates that location acquisition failed because of an exception other than the statuses defined here (=0). */
    public static final int UNDEFINED = 0;

    /** Indicates that location acquisition failed because the device is out of service (=1). */
    public static final int OUT_OF_SERVICE = 1;

    /** Indicates that location acquisition failed because of a timeout (=2). */
    public static final int TIMEOUT = 2;

    /** Indicates that location acquisition failed because of a suspend transition or interruption request (=3). */
    public static final int INTERRUPTED = 3;

    /** Indicates that location acquisition failed because of a user-requested interruption (=4). */
    public static final int USER_ABORT = 4;

    /** Indicates that location acquisition failed because the terminal is in self mode (=5). */
    public static final int SELF_MODE = 5;

    private final int status;

    /**
     * Creates a location exception object without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public LocationException() {
        this(UNDEFINED);
    }

    /**
     * Creates a location exception object with a status value indicating the cause.
     *
     * @param status the status value indicating the cause
     */
    public LocationException(int status) {
        this(status, null);
    }

    /**
     * Creates a location exception object with a status value indicating the cause and a detail message.
     *
     * @param status the status value indicating the cause
     * @param message the detail message
     */
    public LocationException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Gets the status value indicating the cause of the exception.
     *
     * @return the integer status value
     */
    public int getStatus() {
        return status;
    }
}
