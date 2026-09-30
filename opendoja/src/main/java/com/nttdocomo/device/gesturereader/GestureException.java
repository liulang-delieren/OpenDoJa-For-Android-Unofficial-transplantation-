package com.nttdocomo.device.gesturereader;

/**
 * Defines gesture-recognition exceptions.
 */
public class GestureException extends RuntimeException {
    /** Indicates that the status is undefined (=0). */
    public static final int UNDEFINED = 0;

    /** Indicates that the native camera function or code reader function is running (=1). */
    public static final int CAMERA_CONFLICT = 1;

    private final int status;

    /**
     * Creates a gesture-recognition exception object without a detail message.
     * The status becomes {@link #UNDEFINED}.
     */
    public GestureException() {
        this(UNDEFINED);
    }

    /**
     * Creates a gesture-recognition exception object with a status value indicating the cause.
     *
     * @param status the status value to set
     */
    public GestureException(int status) {
        this(status, null);
    }

    /**
     * Creates a gesture-recognition exception object with a status value indicating the cause and a detail message.
     *
     * @param status the status value to set
     * @param message the detail message
     */
    public GestureException(int status, String message) {
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
