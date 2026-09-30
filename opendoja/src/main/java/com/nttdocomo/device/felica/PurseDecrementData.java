package com.nttdocomo.device.felica;

/**
 * Holds purse decrement data.
 */
public final class PurseDecrementData extends PurseData {
    private long decrementData;

    /**
     * Creates data with the specified decrement data.
     * This class is used for purse cashback/decrement access.
     *
     * @param decrementData the decrement data; refer to
     *                      {@link #setDecrementData(long)} for valid values
     * @param execID the execution ID; refer to {@link #setExecID(int)} for
     *               valid values
     * @throws IllegalArgumentException if an argument is outside the supported
     *         range
     */
    public PurseDecrementData(long decrementData, int execID) {
        super(TYPE_PURSE_DECREMENT_DATA, execID);
        setDecrementData(decrementData);
    }

    /**
     * Sets the specified value as the decrement data.
     * It is used by purse services.
     *
     * @param decrementData the decrement data; values from {@code 0} through
     *                      {@code 2^32 - 1} can be specified
     * @throws IllegalArgumentException if {@code decrementData} is negative or
     *         is greater than or equal to {@code 2^32}
     */
    public void setDecrementData(long decrementData) {
        validateUnsignedInt32(decrementData, "decrementData");
        this.decrementData = decrementData;
    }

    /**
     * Returns the decrement data.
     * It is used by purse services.
     *
     * @return the decrement data
     */
    public long getDecrementData() {
        return decrementData;
    }
}
