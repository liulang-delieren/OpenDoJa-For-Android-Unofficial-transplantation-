package com.nttdocomo.system;

/**
 * Defines an access exception for native managed data.
 * The exception detail can be obtained with {@link #getStatus()}.
 */
public class StoreException extends Exception {
    /** Indicates that the status is undefined (=0). */
    public static final int UNDEFINED = 0;

    /** Indicates that the item cannot be added because no free space remains (=1). */
    public static final int STORE_FULL = 1;

    /** Indicates that data for the specified ID could not be found (=2). */
    public static final int NOT_FOUND = 2;

    private final int status;

    /**
     * Creates a {@code StoreException} without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public StoreException() {
        this(UNDEFINED);
    }

    /**
     * Creates a {@code StoreException} with a detail message.
     * The status is set to {@link #UNDEFINED}.
     *
     * @param message the detail message
     */
    public StoreException(String message) {
        this(UNDEFINED, message);
    }

    /**
     * Creates a {@code StoreException} with a status value.
     *
     * @param status the status value to set
     */
    public StoreException(int status) {
        this(status, null);
    }

    /**
     * Creates a {@code StoreException} with a status value and detail message.
     *
     * @param status the status value to set
     * @param message the detail message
     */
    public StoreException(int status, String message) {
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
