package com.nttdocomo.device.gesturereader;

import com.nttdocomo.device.DeviceException;
import com.nttdocomo.lang.UnsupportedOperationException;
import opendoja.host.device.DoJaCameraSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the gesture-recognition function for shake gestures.
 */
public final class ShakeReader extends GestureReader {
    private static final Map<Integer, ShakeReader> READERS = new HashMap<>();

    private ShakeReader(int cameraId) {
        super(cameraId);
    }

    /**
     * Gets the shake-reader object for the specified camera ID.
     * When this method is called for that camera ID for the first time, a
     * shake-reader object is created and returned.
     * After that, a reference to the same object is always returned for the
     * same camera ID.
     *
     * @param id the camera ID
     * @return the shake-reader object
     * @throws UnsupportedOperationException if the specified camera supports
     *         camera capture but does not support gesture recognition
     * @throws IllegalArgumentException if {@code id} is invalid
     * @throws DeviceException if the camera device cannot be secured
     */
    public static ShakeReader getShakeReader(int id) {
        DoJaCameraSupport.validateCameraId(id);
        if (!DoJaCameraSupport.isGestureSupported(id)) {
            throw new UnsupportedOperationException(
                    "Gesture recognition is not supported for camera " + id);
        }
        synchronized (READERS) {
            return READERS.computeIfAbsent(id, ShakeReader::new);
        }
    }

    /**
     * Gets the latest shake-recognition result.
     *
     * @return the amount of vibration, in the range 0 to 511
     * @throws DeviceException if the gesture-recognition engine is in the STOP
     *         state
     * @throws GestureException if the vibration amount cannot be acquired
     */
    public int getShakeData() {
        requireStarted();
        return 0;
    }
}
