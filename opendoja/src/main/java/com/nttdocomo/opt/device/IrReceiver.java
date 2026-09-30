package com.nttdocomo.opt.device;

import com.nttdocomo.device.DeviceException;

import java.io.InputStream;

/**
 * Provides the means to access the handset's native infrared receive
 * function.
 */
public class IrReceiver {
    /** Attribute for the maximum T duration of the infrared signal (=0). */
    public static final int TTIME_MAX = 0;
    /** Attribute for the minimum T duration of the infrared signal (=1). */
    public static final int TTIME_MIN = 1;
    /** Attribute for the maximum start-high duration (=2). */
    public static final int START_HIGH_MAX = 2;
    /** Attribute for the minimum start-high duration (=3). */
    public static final int START_HIGH_MIN = 3;
    /** Attribute for the maximum start-low duration (=4). */
    public static final int START_LOW_MAX = 4;
    /** Attribute for the minimum start-low duration (=5). */
    public static final int START_LOW_MIN = 5;
    /** Attribute for the maximum data-0 high duration (=6). */
    public static final int DATA0_HIGH_MAX = 6;
    /** Attribute for the minimum data-0 high duration (=7). */
    public static final int DATA0_HIGH_MIN = 7;
    /** Attribute for the maximum data-0 low duration (=8). */
    public static final int DATA0_LOW_MAX = 8;
    /** Attribute for the minimum data-0 low duration (=9). */
    public static final int DATA0_LOW_MIN = 9;
    /** Attribute for the maximum data-1 high duration (=10). */
    public static final int DATA1_HIGH_MAX = 10;
    /** Attribute for the minimum data-1 high duration (=11). */
    public static final int DATA1_HIGH_MIN = 11;
    /** Attribute for the maximum data-1 low duration (=12). */
    public static final int DATA1_LOW_MAX = 12;
    /** Attribute for the minimum data-1 low duration (=13). */
    public static final int DATA1_LOW_MIN = 13;
    /** Attribute for the maximum stop-high duration (=14). */
    public static final int STOP_HIGH_MAX = 14;
    /** Attribute for the minimum stop-high duration (=15). */
    public static final int STOP_HIGH_MIN = 15;
    /** Attribute for the maximum stop-low duration (=16). */
    public static final int STOP_LOW_MAX = 16;
    /** Attribute for the minimum stop-low duration (=17). */
    public static final int STOP_LOW_MIN = 17;

    /**
     * Applications cannot create instances of this class directly.
     */
    protected IrReceiver() {
    }

    /**
     * Gets the infrared-receive object.
     *
     * @return the infrared-receive object
     * @throws DeviceException never thrown by this host
     */
    public static synchronized IrReceiver getIrReceiver() {
        return _OptionalDeviceSupport.irReceiver();
    }

    /**
     * Gets the input stream used to read received infrared data.
     *
     * @return the input stream used to read received infrared data
     */
    public synchronized InputStream getInputStream() {
        return _OptionalDeviceSupport.irInputStream();
    }

    /**
     * Starts infrared reception.
     *
     */
    public void receive() {
        _OptionalDeviceSupport.irReceive();
    }

    /**
     * Stops infrared reception.
     *
     */
    public void cancel() {
        _OptionalDeviceSupport.irCancel();
    }

    /**
     * Sets an attribute related to infrared reception.
     *
     * @param attr the attribute kind
     * @param value the attribute value
     */
    public void setAttribute(int attr, int value) {
        _OptionalDeviceSupport.irSetAttribute(attr, value);
    }
}
