package com.nttdocomo.device.felica;

import com.nttdocomo.util.EventListener;

import java.util.Hashtable;

/**
 * Defines a listener for events concerning continuous data transfer by ad hoc communication.
 */
public interface FelicaAdhocListener extends EventListener {
    boolean requestReceived(Hashtable receivedParams);
}
