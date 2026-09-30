package com.nttdocomo.opt.device;

/**
 * Provides the means to access the pulse-meter function.
 */
public class Pulsemeter {
    /** Status value meaning measurement is operating normally (=0). */
    public static final int STATUS_NONE = 0;
    /** Status value meaning it is too dark (=1). */
    public static final int STATUS_DARK = 1;
    /** Status value meaning it is too bright (=2). */
    public static final int STATUS_BRIGHT = 2;
    /** Status value meaning the finger position is poor (=3). */
    public static final int STATUS_BAD_POSITION = 3;
    /** Status value meaning no finger is placed (=4). */
    public static final int STATUS_NO_FINGER = 4;
    /** Status value meaning measurement is stopped (=5). */
    public static final int STATUS_STOP = 5;

    Pulsemeter() {
    }

    /**
     * Gets the pulse-meter object for the specified camera ID.
     *
     * @param id the camera ID
     * @return the pulse-meter object for the specified camera ID
     */
    public static Pulsemeter getPulsemeter(int id) {
        return _OptionalDeviceSupport.pulsemeter(id);
    }

    /**
     * Starts pulse measurement.
     *
     */
    public void start() {
        _OptionalDeviceSupport.startPulsemeter(this);
    }

    /**
     * Stops pulse measurement.
     *
     */
    public void stop() {
        _OptionalDeviceSupport.stopPulsemeter(this);
    }

    /**
     * Gets the pulse-meter status.
     *
     * @return the pulse-meter status
     */
    public int getStatus() {
        return _OptionalDeviceSupport.pulsemeterStatus(this);
    }

    /**
     * Gets the measured pulse rate.
     *
     * @return the measured pulse rate, or {@code -1} until a value is ready
     */
    public int getPulserate() {
        return _OptionalDeviceSupport.pulsemeterRate(this);
    }
}
