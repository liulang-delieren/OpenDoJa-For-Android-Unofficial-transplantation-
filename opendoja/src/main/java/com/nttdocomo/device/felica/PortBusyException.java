package com.nttdocomo.device.felica;

import java.io.IOException;

/**
 * Indicates that the FeliCa communication port is busy.
 */
public class PortBusyException extends IOException {
    /**
     * Creates a {@code PortBusyException} without a detail message.
     */
    public PortBusyException() {
    }

    /**
     * Creates a {@code PortBusyException} with a detail message.
     *
     * @param message the detail message
     */
    public PortBusyException(String message) {
        super(message);
    }
}
