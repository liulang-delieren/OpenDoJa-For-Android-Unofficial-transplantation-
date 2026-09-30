package com.nttdocomo.device.felica;

/**
 * Base class of FeliCa data objects.
 */
public abstract class FelicaData {
    /**
     * Represents block data for random and cyclic services (=0).
     */
    public static final int TYPE_DIRECT_DATA = 0;
    /**
     * Represents purse cashback/decrement-service data (cashback) (=1).
     */
    public static final int TYPE_PURSE_CASHBACK_DATA = 1;
    /**
     * Represents purse cashback/decrement-service data (decrement) (=2).
     */
    public static final int TYPE_PURSE_DECREMENT_DATA = 2;
    /**
     * Represents data for the purse direct service (=3).
     */
    public static final int TYPE_PURSE_DIRECT_DATA = 3;

    private final int dataType;

    FelicaData(int dataType) {
        this.dataType = dataType;
    }

    /**
     * Returns the type of this block data.
     *
     * @return the block-data type
     */
    public final int getDataType() {
        return dataType;
    }
}
