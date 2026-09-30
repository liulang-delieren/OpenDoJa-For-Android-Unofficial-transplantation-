package com.nttdocomo.device;

/**
 * Defines device exceptions.
 * This exception is mainly thrown to indicate that a class in the device
 * package has raised a runtime exception.
 */
public class DeviceException extends RuntimeException {
    /** Indicates that the status is undefined (=0). */
    public static final int UNDEFINED = 0;

    /** Indicates that the state is invalid (=1). */
    public static final int ILLEGAL_STATE = 1;

    /** Indicates that resources cannot be secured (=2). */
    public static final int NO_RESOURCES = 2;

    /** Indicates that a resource is busy (=3). */
    public static final int BUSY_RESOURCE = 3;

    /** Indicates that the device cannot be used because of a race condition or similar state (=4). */
    public static final int RACE_CONDITION = 4;

    /** Indicates that device processing failed because of an interruption request (=5). */
    public static final int INTERRUPTED = 5;

    private final int status;

    /**
     * Creates a device exception object without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public DeviceException() {
        this(UNDEFINED);
    }

    /**
     * Creates a device exception object with a status value indicating the cause.
     *
     * @param status the status value to set
     */
    public DeviceException(int status) {
        this(status, null);
    }

    /**
     * Creates a device exception object with a status value indicating the cause and a detail message.
     *
     * @param status the status value to set
     * @param message the detail message
     */
    public DeviceException(int status, String message) {
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
