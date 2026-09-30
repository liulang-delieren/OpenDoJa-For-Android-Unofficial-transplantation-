package com.nttdocomo.ui;

/**
 * Defines a UI exception.
 * It is thrown to indicate that a component or other object in the UI package
 * has caused a runtime exception.
 * The detail of the exception can be obtained with {@link #getStatus()}.
 */
public class UIException extends RuntimeException {
    /**
     * Indicates that the status is undefined ({@code =0}).
     * This value is set for exceptions whose status does not need to be
     * specified.
     */
    public static final int UNDEFINED = 0;
    /**
     * Indicates that the state is illegal ({@code =1}).
     */
    public static final int ILLEGAL_STATE = 1;
    /**
     * Indicates that a resource cannot be reserved ({@code =2}).
     */
    public static final int NO_RESOURCES = 2;
    /**
     * Indicates that a resource is in use ({@code =3}).
     */
    public static final int BUSY_RESOURCE = 3;
    /**
     * Indicates that the format is unsupported ({@code =4}).
     */
    public static final int UNSUPPORTED_FORMAT = 4;
    /**
     * The first status-code value used by this exception ({@code =0}).
     * It is the first value used by this exception including reserved values.
     */
    protected static final int STATUS_FIRST = 0;
    /**
     * The last status-code value used by this exception ({@code =63}).
     * It is the last value used by this exception including reserved values.
     */
    protected static final int STATUS_LAST = 63;

    private final int status;

    /**
     * Creates a UI exception object without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public UIException() {
        this(UNDEFINED);
    }

    /**
     * Creates a UI exception object with a status value.
     *
     * @param status the status value to set
     */
    public UIException(int status) {
        this(status, null);
    }

    /**
     * Creates a UI exception object with a status value and a detail message.
     *
     * @param status the status value to set
     * @param message the detail message
     */
    public UIException(int status, String message) {
        super(message);
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
