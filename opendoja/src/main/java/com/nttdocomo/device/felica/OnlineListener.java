package com.nttdocomo.device.felica;

import com.nttdocomo.util.EventListener;

/**
 * Defines a listener for events related to online processing of the FeliCa feature.
 */
public interface OnlineListener extends EventListener {
    int TYPE_UNEXPECTED_ERROR = 0x8000;
    int TYPE_INTERRUPTED_ERROR = 0x8001;

    byte[] deviceOperationRequest(int deviceID, String param, byte[] data);

    void onlineError(int type, String message);

    void onlineFinished(int status);
}
