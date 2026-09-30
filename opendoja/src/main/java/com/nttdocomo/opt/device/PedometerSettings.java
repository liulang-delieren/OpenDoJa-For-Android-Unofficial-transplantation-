package com.nttdocomo.opt.device;

/**
 * Defines the settings for the pedometer function.
 */
public class PedometerSettings {
    /** Attribute kind representing the minimum settable height, in millimeters (=0). */
    public static final int ATTR_HEIGHT_MIN = 0;
    /** Attribute kind representing the maximum settable height, in millimeters (=1). */
    public static final int ATTR_HEIGHT_MAX = 1;
    /** Attribute kind representing the minimum settable weight, in grams (=2). */
    public static final int ATTR_WEIGHT_MIN = 2;
    /** Attribute kind representing the maximum settable weight, in grams (=3). */
    public static final int ATTR_WEIGHT_MAX = 3;

    private PedometerSettings() {
    }

    /**
     * Sets the body height used by the pedometer.
     *
     * @param height the height, in millimeters
     */
    public static void setBodyHeight(int height) {
        _OptionalDeviceSupport.setBodyHeight(height);
    }

    /**
     * Sets the body weight used by the pedometer.
     *
     * @param weight the weight, in grams
     */
    public static void setBodyWeight(int weight) {
        _OptionalDeviceSupport.setBodyWeight(weight);
    }

    /**
     * Gets a pedometer-settings attribute value.
     *
     * @param attr the attribute kind
     * @return the requested pedometer-setting attribute value
     */
    public static int getAttribute(int attr) {
        return _OptionalDeviceSupport.pedometerSetting(attr);
    }
}
