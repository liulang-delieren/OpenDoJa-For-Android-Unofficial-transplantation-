package com.nttdocomo.device.felica;

import com.nttdocomo.util.EventListener;

/**
 * Defines a listener for events when push notification is received from an external reader/writer.
 */
public interface FelicaPushListener extends EventListener {
    void pushReceived(byte[] data);
}
