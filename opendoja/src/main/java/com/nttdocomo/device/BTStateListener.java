package com.nttdocomo.device;

import com.nttdocomo.util.EventListener;

/**
 * Defines a listener for events concerning changes in Bluetooth connection state.
 */
public interface BTStateListener extends EventListener {
    /** Indicates that the Bluetooth connection has been disconnected (=0). */
    int DISCONNECT = 0;

    /**
     * Receives notification of a state change for a connected Bluetooth link.
     *
     * @param state the transitioned state; {@link #DISCONNECT} is passed
     */
    void stateChanged(int state);
}
