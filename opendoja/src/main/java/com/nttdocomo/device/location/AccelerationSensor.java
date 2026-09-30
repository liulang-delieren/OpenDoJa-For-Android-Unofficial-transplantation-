package com.nttdocomo.device.location;

import com.nttdocomo.device.DeviceException;
import com.nttdocomo.lang.UnsupportedOperationException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Provides acceleration-sensor functions.
 */
public class AccelerationSensor {
    /** Measurement-data type representing X-axis acceleration (=1). */
    public static final int ACCELERATION_X = 1;

    /** Measurement-data type representing Y-axis acceleration (=2). */
    public static final int ACCELERATION_Y = 2;

    /** Measurement-data type representing Z-axis acceleration (=3). */
    public static final int ACCELERATION_Z = 3;

    /** Measurement-data type representing roll angle (=4). */
    public static final int ROLL = 4;

    /** Measurement-data type representing pitch angle (=5). */
    public static final int PITCH = 5;

    /** Measurement-data type representing the main-screen orientation (=6). */
    public static final int SCREEN_ORIENTATION = 6;

    /** Position value representing a double tap on the left side of the terminal (=0). */
    public static final int DOUBLE_TAP_LEFT = 0;

    /** Position value representing a double tap on the right side of the terminal (=1). */
    public static final int DOUBLE_TAP_RIGHT = 1;

    /** Position value representing a double tap on the LCD side of the terminal (=2). */
    public static final int DOUBLE_TAP_FRONT = 2;

    /** Position value representing a double tap on the back side of the terminal (=3). */
    public static final int DOUBLE_TAP_BACK = 3;

    /** Event type representing a change in screen orientation (=1). */
    public static final int EVENT_SCREEN_ORIENTATION = 1;

    /** Event type representing a double-tap action (=2). */
    public static final int EVENT_DOUBLE_TAP = 2;

    private static final AccelerationSensor INSTANCE = new AccelerationSensor();

    private final AccelerationData currentData = new AccelerationData();
    private final AccelerationData bufferedData = new AccelerationData();
    private final AccelerationData latestData = new AccelerationData();
    private final Deque<int[]> buffer = new ArrayDeque<>();

    private boolean measuring;
    private boolean eventDetecting;
    private AccelerationEventListener eventListener;
    private ScheduledFuture<?> measurementTask;
    private ScheduledFuture<?> eventTask;

    AccelerationSensor() {
    }

    /**
     * Returns the acceleration-sensor instance.
     * If the implementation does not support the acceleration-sensor function,
     * an exception is thrown.
     *
     * @return the acceleration-sensor instance
     * @throws UnsupportedOperationException if the acceleration-sensor function
     *         is not supported
     */
    public static AccelerationSensor getAccelerationSensor() {
        if (!_LocationSupport.accelerationSupported()) {
            throw new UnsupportedOperationException("Acceleration sensor is not supported by openDoJa");
        }
        return INSTANCE;
    }

    /**
     * Gets the types of measurement data supported by the implementation.
     *
     * @return a copy of the supported measurement-data types
     */
    public int[] getAvailableData() {
        return _LocationSupport.availableAccelerationData().clone();
    }

    /**
     * Gets the minimum value for the specified measurement-data type.
     *
     * @param type the measurement-data type
     * @return the minimum value for that type
     * @throws IllegalArgumentException if {@code type} is invalid or unsupported
     */
    public int getMinDataValue(int type) {
        ensureSupportedDataType(type);
        return _LocationSupport.minAccelerationValue(type);
    }

    /**
     * Gets the maximum value for the specified measurement-data type.
     *
     * @param type the measurement-data type
     * @return the maximum value for that type
     * @throws IllegalArgumentException if {@code type} is invalid or unsupported
     */
    public int getMaxDataValue(int type) {
        ensureSupportedDataType(type);
        return _LocationSupport.maxAccelerationValue(type);
    }

    /**
     * Measures the current acceleration and gets the result as a one-shot
     * measurement.
     *
     * @return the one-shot measurement result
     * @throws DeviceException if periodic measurement or event detection is in
     *         progress ({@link DeviceException#ILLEGAL_STATE})
     */
    public AccelerationData getCurrentData() {
        if (measuring) {
            throw new DeviceException(DeviceException.ILLEGAL_STATE,
                    "Acceleration measurement is already in progress");
        }
        buffer.clear();
        int[] sample = _LocationSupport.accelerationSample();
        currentData.update(1,
                singleValue(sample, ACCELERATION_X, 0),
                singleValue(sample, ACCELERATION_Y, 1),
                singleValue(sample, ACCELERATION_Z, 2),
                singleValue(sample, ROLL, 3),
                singleValue(sample, PITCH, 4),
                singleValue(sample, SCREEN_ORIENTATION, 5));
        return currentData;
    }

    /**
     * Starts periodic measurement.
     *
     * @param interval the periodic-measurement interval in milliseconds
     * @throws DeviceException if periodic measurement or event detection is in
     *         progress ({@link DeviceException#ILLEGAL_STATE})
     * @throws IllegalArgumentException if {@code interval} is 0 or less
     */
    public void start(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("interval out of range: " + interval);
        }
        if (measuring) {
            throw new DeviceException(DeviceException.ILLEGAL_STATE,
                    "Acceleration measurement is already in progress");
        }
        disposeData();
        int resolution = getIntervalResolution();
        int effectiveInterval = ((interval + resolution - 1) / resolution) * resolution;
        measuring = true;
        measurementTask = _LocationSupport.EXECUTOR.scheduleAtFixedRate(
                this::capturePeriodicSample,
                effectiveInterval,
                effectiveInterval,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Stops periodic measurement.
     * If it is not in progress, nothing happens.
     */
    public void stop() {
        measuring = false;
        if (measurementTask != null) {
            measurementTask.cancel(false);
            measurementTask = null;
        }
    }

    /**
     * Gets the resolution of the periodic-measurement interval, in
     * milliseconds.
     *
     * @return the interval resolution in milliseconds
     */
    public int getIntervalResolution() {
        return _LocationSupport.accelerationIntervalResolution();
    }

    /**
     * Gets the maximum number of data items that can be accumulated
     * internally during periodic measurement.
     *
     * @return the maximum number of accumulated data items
     */
    public int getMaxDataSize() {
        return _LocationSupport.maxAccelerationDataSize();
    }

    /**
     * Gets the number of data items currently stored in the periodic
     * measurement buffer.
     *
     * @return the number of accumulated data items
     */
    public int getDataSize() {
        return buffer.size();
    }

    /**
     * Gets all measurement results from the periodic measurement buffer.
     *
     * @return an object containing all periodic measurement data
     */
    public AccelerationData getData() {
        updateDataObject(bufferedData, buffer);
        buffer.clear();
        return bufferedData;
    }

    /**
     * Gets the most recent measurement result from the periodic measurement
     * buffer.
     *
     * @return an object containing the most recent periodic measurement data
     */
    public AccelerationData peekLatestData() {
        if (buffer.isEmpty()) {
            latestData.update(0,
                    emptyOrNull(ACCELERATION_X),
                    emptyOrNull(ACCELERATION_Y),
                    emptyOrNull(ACCELERATION_Z),
                    emptyOrNull(ROLL),
                    emptyOrNull(PITCH),
                    emptyOrNull(SCREEN_ORIENTATION));
        } else {
            int[] sample = buffer.peekLast();
            latestData.update(1,
                    singleValue(sample, ACCELERATION_X, 0),
                    singleValue(sample, ACCELERATION_Y, 1),
                    singleValue(sample, ACCELERATION_Z, 2),
                    singleValue(sample, ROLL, 3),
                    singleValue(sample, PITCH, 4),
                    singleValue(sample, SCREEN_ORIENTATION, 5));
        }
        return latestData;
    }

    /**
     * Clears the accumulation buffer for periodic measurement.
     */
    public void disposeData() {
        buffer.clear();
    }

    /**
     * Gets the event types supported by the event-detection function.
     *
     * @return a copy of the supported event types, or {@code null} if the
     *         event-detection function is not supported
     */
    public int[] getAvailableEvent() {
        int[] events = _LocationSupport.availableAccelerationEvents();
        return events == null ? null : events.clone();
    }

    /**
     * Requests event detection from the acceleration-sensor function.
     *
     * @param event the event to detect
     * @throws UnsupportedOperationException if the event-detection function is
     *         not supported
     * @throws DeviceException if periodic measurement is in progress, event
     *         detection is already in progress, or a listener is not set
     *         ({@link DeviceException#ILLEGAL_STATE})
     * @throws IllegalArgumentException if the specified event is unsupported
     */
    public void startEventDetection(int event) {
        int[] availableEvents = _LocationSupport.availableAccelerationEvents();
        if (availableEvents == null) {
            throw new UnsupportedOperationException("Acceleration sensor event detection is not supported by openDoJa");
        }
        if (measuring || eventDetecting || eventListener == null) {
            throw new DeviceException(DeviceException.ILLEGAL_STATE,
                    "Event detection cannot be started in the current state");
        }
        int supportedMask = 0;
        for (int availableEvent : availableEvents) {
            supportedMask |= availableEvent;
        }
        if (event <= 0 || (event & ~supportedMask) != 0) {
            throw new IllegalArgumentException("Unsupported event mask: " + event);
        }
        disposeData();
        eventDetecting = true;
        eventTask = _LocationSupport.EXECUTOR.schedule(() -> fireConfiguredEvents(event), 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops event detection by the acceleration-sensor function.
     * If event detection is already stopped, nothing happens.
     */
    public void stopEventDetection() {
        eventDetecting = false;
        if (eventTask != null) {
            eventTask.cancel(false);
            eventTask = null;
        }
    }

    /**
     * Sets the event listener used by the event-detection function.
     * Specify {@code null} to clear the currently set listener.
     *
     * @param listener the event listener
     * @throws DeviceException if event detection is in progress
     *         ({@link DeviceException#ILLEGAL_STATE})
     */
    public void setEventListener(AccelerationEventListener listener) {
        if (eventDetecting) {
            throw new DeviceException(DeviceException.ILLEGAL_STATE,
                    "Event detection is already in progress");
        }
        this.eventListener = listener;
    }

    private void capturePeriodicSample() {
        int[] sample = _LocationSupport.accelerationSample();
        if (buffer.size() >= getMaxDataSize()) {
            buffer.removeFirst();
        }
        buffer.addLast(sample);
    }

    private void fireConfiguredEvents(int mask) {
        AccelerationEventListener listener = eventListener;
        if (!eventDetecting || listener == null) {
            return;
        }
        if ((mask & EVENT_SCREEN_ORIENTATION) != 0) {
            listener.actionPerformed(this, EVENT_SCREEN_ORIENTATION, _LocationSupport.screenOrientationEventValue());
        }
        if ((mask & EVENT_DOUBLE_TAP) != 0) {
            listener.actionPerformed(this, EVENT_DOUBLE_TAP, _LocationSupport.doubleTapEventValue());
        }
    }

    private void updateDataObject(AccelerationData target, Deque<int[]> source) {
        int size = source.size();
        int[] x = new int[size];
        int[] y = new int[size];
        int[] z = new int[size];
        int[] roll = new int[size];
        int[] pitch = new int[size];
        int[] screen = new int[size];
        int index = 0;
        for (int[] sample : source) {
            x[index] = sample[0];
            y[index] = sample[1];
            z[index] = sample[2];
            roll[index] = sample[3];
            pitch[index] = sample[4];
            screen[index] = sample[5];
            index++;
        }
        target.update(size,
                supportedOrNull(ACCELERATION_X, x),
                supportedOrNull(ACCELERATION_Y, y),
                supportedOrNull(ACCELERATION_Z, z),
                supportedOrNull(ROLL, roll),
                supportedOrNull(PITCH, pitch),
                supportedOrNull(SCREEN_ORIENTATION, screen));
    }

    private void ensureSupportedDataType(int type) {
        if (type < ACCELERATION_X || type > SCREEN_ORIENTATION) {
            throw new IllegalArgumentException("type out of range: " + type);
        }
        for (int available : _LocationSupport.availableAccelerationData()) {
            if (available == type) {
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported data type: " + type);
    }

    private boolean supportsType(int type) {
        for (int available : _LocationSupport.availableAccelerationData()) {
            if (available == type) {
                return true;
            }
        }
        return false;
    }

    private int[] singleValue(int[] sample, int type, int index) {
        if (!supportsType(type)) {
            return null;
        }
        return new int[]{sample[index]};
    }

    private int[] emptyOrNull(int type) {
        return supportsType(type) ? new int[0] : null;
    }

    private int[] supportedOrNull(int type, int[] values) {
        return supportsType(type) ? values : null;
    }
}
