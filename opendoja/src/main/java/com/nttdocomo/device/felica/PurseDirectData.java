package com.nttdocomo.device.felica;

/**
 * Holds purse direct-write data.
 */
public final class PurseDirectData extends PurseData {
    private static final int USER_DATA_LENGTH = 6;

    private long purseData;
    private long cashbackData;
    private byte[] userData;

    /**
     * Creates a {@code PurseDirectData} with the specified purse data,
     * cashback data, user data, and execution ID.
     *
     * @param purseData the purse data; refer to {@link #setPurseData(long)} for
     *                  valid values
     * @param cashbackData the cashback data; refer to
     *                     {@link #setCashbackData(long)} for valid values
     * @param userData the user data; refer to {@link #setUserData(byte[])} for
     *                 valid values
     * @param execID the execution ID; refer to {@link #setExecID(int)} for
     *               valid values
     * @throws NullPointerException if {@code userData} is {@code null}
     * @throws IllegalArgumentException if a numeric argument is outside the
     *         supported range
     */
    public PurseDirectData(long purseData, long cashbackData, byte[] userData, int execID) {
        super(TYPE_PURSE_DIRECT_DATA, execID);
        setPurseData(purseData);
        setCashbackData(cashbackData);
        setUserData(userData);
    }

    /**
     * Returns the purse data.
     * It is used by purse services.
     *
     * @return the purse data
     */
    public long getPurseData() {
        return purseData;
    }

    /**
     * Sets the specified value as the purse data.
     * It is used by purse services.
     *
     * @param purseData the purse data; values from {@code 0} through
     *                  {@code 2^32 - 1} can be specified
     * @throws IllegalArgumentException if {@code purseData} is negative or is
     *         greater than or equal to {@code 2^32}
     */
    public void setPurseData(long purseData) {
        validateUnsignedInt32(purseData, "purseData");
        this.purseData = purseData;
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

    /**
     * Returns the user data.
     * It is used by purse services.
     *
     * @return a copy of the user data
     */
    public byte[] getUserData() {
        return userData.clone();
    }

    /**
     * Sets the specified value as the user data.
     * If the length is shorter than 6 bytes, the remainder is zero padded.
     * Any bytes beyond 6 are ignored.
     *
     * @param userData the user data
     * @throws NullPointerException if {@code userData} is {@code null}
     */
    public void setUserData(byte[] userData) {
        if (userData == null) {
            throw new NullPointerException("userData");
        }
        this.userData = new byte[USER_DATA_LENGTH];
        System.arraycopy(userData, 0, this.userData, 0, java.lang.Math.min(USER_DATA_LENGTH, userData.length));
    }
}
