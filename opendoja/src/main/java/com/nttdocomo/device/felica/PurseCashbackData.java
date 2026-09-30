package com.nttdocomo.device.felica;

/**
 * Holds purse cashback data.
 */
public final class PurseCashbackData extends PurseData {
    private long cashbackData;

    /**
     * Creates data with the specified cashback data.
     * This class is used for purse cashback/decrement access.
     *
     * @param cashbackData the cashback data; refer to
     *                     {@link #setCashbackData(long)} for valid values
     * @param execID the execution ID; refer to {@link #setExecID(int)} for
     *               valid values
     * @throws IllegalArgumentException if an argument is outside the supported
     *         range
     */
    public PurseCashbackData(long cashbackData, int execID) {
        super(TYPE_PURSE_CASHBACK_DATA, execID);
        setCashbackData(cashbackData);
    }

    /**
     * Sets the specified value as the cashback data.
     * It is used for cashback processing.
     *
     * @param cashbackData the cashback data; values from {@code 0} through
     *                     {@code 2^32 - 1} can be specified
     * @throws IllegalArgumentException if {@code cashbackData} is negative or
     *         is greater than or equal to {@code 2^32}
     */
    public void setCashbackData(long cashbackData) {
        validateUnsignedInt32(cashbackData, "cashbackData");
        this.cashbackData = cashbackData;
    }

    /**
     * Returns the cashback data.
     * It is used by purse services.
     *
     * @return the cashback data
     */
    public long getCashbackData() {
        return cashbackData;
    }
}
