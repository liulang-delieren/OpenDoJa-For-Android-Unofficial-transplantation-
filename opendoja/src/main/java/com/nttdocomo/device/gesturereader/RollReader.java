package com.nttdocomo.device.gesturereader;

import com.nttdocomo.device.DeviceException;
import com.nttdocomo.lang.UnsupportedOperationException;
import opendoja.host.device.DoJaCameraSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the gesture-recognition function for roll gestures.
 */
public final class RollReader extends GestureReader {
    private static final long SAME_VALUE_THRESHOLD_NANOS = 100_000_000L;
    private static final Map<Integer, RollReader> READERS = new HashMap<>();

    private final RollData rollData = new RollData();
    private long lastSampleTimeNanos;

    private RollReader(int cameraId) {
        super(cameraId);
    }

    /**
     * Gets the roll-reader object for the specified camera ID.
     * When this method is called for that camera ID for the first time, a
     * roll-reader object is created and returned.
     * After that, a reference to the same object is always returned for the
     * same camera ID.
     *
     * @param id the camera ID
     * @return the roll-reader object
     * @throws UnsupportedOperationException if the specified camera supports
     *         camera capture but does not support gesture recognition
     * @throws IllegalArgumentException if {@code id} is invalid
     * @throws DeviceException if the camera device cannot be secured
     */
    public static RollReader getRollReader(int id) {
        DoJaCameraSupport.validateCameraId(id);
        if (!DoJaCameraSupport.isGestureSupported(id)) {
            throw new UnsupportedOperationException(
                    "Gesture recognition is not supported for camera " + id);
        }
        synchronized (READERS) {
            return READERS.computeIfAbsent(id, RollReader::new);
        }
    }

    /**
     * Resets the reference image.
     *
     * @throws DeviceException if the gesture-recognition engine is in the STOP
     *         state
     * @throws GestureException if the reference image cannot be reset
     */
    public void recenter() {
        requireStarted();
        lastSampleTimeNanos = 0L;
        rollData.update(RollData.QUALITY_RELIABLE, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Gets the {@link RollData} object that holds the latest roll-recognition
     * result.
     * The same object is always returned after its contents are updated.
     *
     * @return the roll-data object
     * @throws DeviceException if the gesture-recognition engine is in the STOP
     *         state
     * @throws GestureException if the recognition result cannot be acquired
     */
    public RollData getRollData() {
        requireStarted();
        long now = System.nanoTime();
        int quality = lastSampleTimeNanos != 0L
                && now - lastSampleTimeNanos < SAME_VALUE_THRESHOLD_NANOS
                ? RollData.QUALITY_SAME_VALUE
                : RollData.QUALITY_RELIABLE;
        lastSampleTimeNanos = now;
        rollData.update(quality, 0.0f, 0.0f, 0.0f, 0.0f);
        return rollData;
    }

    @Override
    void onStart() {
        lastSampleTimeNanos = 0L;
        rollData.update(RollData.QUALITY_RELIABLE, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    void onStop() {
        lastSampleTimeNanos = 0L;
    }
}
