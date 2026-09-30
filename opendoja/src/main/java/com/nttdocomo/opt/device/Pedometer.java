package com.nttdocomo.opt.device;

/**
 * Provides the means to access the pedometer function.
 */
public class Pedometer {
    /** Attribute kind representing the pedometer measurement state (=0). */
    public static final int DEV_PEDOMETER = 0;
    /** Attribute value meaning measurement is off (=0). */
    public static final int ATTR_PEDOMETER_OFF = 0;
    /** Attribute value meaning measurement is on (=1). */
    public static final int ATTR_PEDOMETER_ON = 1;

    /**
     * Applications cannot create instances of this class directly.
     */
    protected Pedometer() {
    }

    /**
     * Gets the pedometer object.
     *
     * @return the pedometer object
     */
    public static Pedometer getPedometer() {
        return _OptionalDeviceSupport.pedometer();
    }

    /**
     * Gets a pedometer attribute value.
     *
     * @param attr the attribute kind
     * @return the attribute value, or {@code -1} if the attribute is unknown
     */
    public int getAttribute(int attr) {
        return _OptionalDeviceSupport.pedometerAttribute(attr);
    }

    /**
     * Gets the number of pedometer-history entries currently held.
     *
     * @return the number of pedometer-history entries currently held
     */
    public int getCount() {
        return _OptionalDeviceSupport.pedometerCount();
    }

    /**
     * Gets the total step count currently held by the pedometer.
     *
     * @return the total step count currently held by the pedometer
     */
    public int getTotalSteps() {
        return _OptionalDeviceSupport.pedometerTotalSteps();
    }

    /**
     * Gets the total walking distance currently held by the pedometer.
     *
     * @return the total walking distance currently held by the pedometer
     */
    public int getTotalDistance() {
        return _OptionalDeviceSupport.pedometerTotalDistance();
    }

    /**
     * Gets today's pedometer data.
     *
     * @return today's pedometer data
     */
    public PedometerData getTodayData() {
        return _OptionalDeviceSupport.todayPedometerData();
    }

    /**
     * Gets all pedometer-history data.
     *
     * @return all pedometer-history data
     */
    public PedometerData[] getData() {
        return _OptionalDeviceSupport.pedometerData();
    }

    /**
     * Gets a range of pedometer-history data.
     *
     * @param index the zero-based index from today
     * @param articles the number of entries to acquire
     * @return the requested range of pedometer-history data
     */
    public PedometerData[] getData(int index, int articles) {
        return _OptionalDeviceSupport.pedometerData(index, articles);
    }
}
