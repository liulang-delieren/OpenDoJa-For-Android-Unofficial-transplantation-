package com.nttdocomo.device.gesturereader;

import opendoja.host.device.DoJaCameraSupport;

/**
 * Defines a class representing the gesture-recognition function.
 * Gesture recognition uses the handset's native camera device to recognize a
 * captured image and obtain movement information from it.
 */
public abstract class GestureReader {
    private final int cameraId;

    /**
     * Creates a gesture-recognition object.
     */
    public GestureReader() {
        this(-1);
    }

    GestureReader(int cameraId) {
        this.cameraId = cameraId;
    }

    final int cameraId() {
        return cameraId;
    }

    /**
     * Places the gesture-recognition engine into the START state.
     * If this method is called again for the same object while it is already in
     * the START state, the later call is ignored.
     */
    public void start() {
        DoJaCameraSupport.startGesture(this, cameraId);
        onStart();
    }

    /**
     * Places the gesture-recognition engine into the STOP state.
     * If the engine is already in the STOP state, the call is ignored.
     */
    public void stop() {
        if (DoJaCameraSupport.isGestureStarted(this)) {
            onStop();
            DoJaCameraSupport.stopGesture(this);
        }
    }

    void requireStarted() {
        if (!DoJaCameraSupport.isGestureStarted(this)) {
            throw new com.nttdocomo.device.DeviceException(
                    com.nttdocomo.device.DeviceException.ILLEGAL_STATE,
                    "GestureReader is in the STOP state");
        }
    }

    void onStart() {
    }

    void onStop() {
    }
}
