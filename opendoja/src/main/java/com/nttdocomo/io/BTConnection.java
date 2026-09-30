package com.nttdocomo.io;

import com.nttdocomo.device.BTStateListener;

/**
 * Defines a Bluetooth connection.
 */
public interface BTConnection {
    /**
     * Disconnects the connection to the external device and releases resources.
     */
    void close();

    /**
     * Defines a listener for events concerning changes in the state of the connected Bluetooth link.
     *
     * @param listener the listener object to set, or {@code null} to clear it
     */
    void setBTStateListener(BTStateListener listener);
}
