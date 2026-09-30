package com.nttdocomo.device.felica;

import java.io.IOException;

/**
 * Indicates that an online FeliCa connection request was refused.
 */
public class ConnectionRefusedException extends IOException {
    /** Status indicating that the specified application does not exist (=1). */
    public static final int APPLICATION_NOT_EXIST = 1;
    /** Status indicating that another application is already active (=2). */
    public static final int BUSY_RESOURCE = 2;
    /** Status indicating refusal by user-setting restrictions (=3). */
    public static final int REFUSED_BY_USER_SETTING = 3;
    /** Status indicating refusal by ADF settings (=4). */
    public static final int REFUSED_BY_ADF_SETTING = 4;
    /** Status indicating refusal by the destination application (=5). */
    public static final int REFUSED_BY_APPLICATION = 5;
    /** Status indicating that the destination application is in a race condition (=6). */
    public static final int RACE_CONDITION = 6;
    /** Status indicating an undefined refusal reason (=0). */
    public static final int UNDEFINED = 0;

    private final int status;

    /**
     * Creates a connection-refused exception object without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public ConnectionRefusedException() {
        this(UNDEFINED);
    }

    /**
     * Creates a connection-refused exception object with a status value.
     *
     * @param status the status value indicating the refusal reason
     */
    public ConnectionRefusedException(int status) {
        this(status, null);
    }

    /**
     * Creates a connection-refused exception object with a status value and a
     * detail message.
     *
     * @param status the status value indicating the refusal reason
     * @param message the detail message
     */
    public ConnectionRefusedException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Gets the status value indicating the refusal reason.
     *
     * @return the status value
     */
    public int getStatus() {
        return status;
    }
}
