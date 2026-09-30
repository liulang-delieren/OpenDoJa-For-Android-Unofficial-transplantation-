package com.nttdocomo.device.felica;

/**
 * Holds FeliCa status flags.
 */
public final class FelicaStatus {
    private final byte statusFlag1;
    private final byte statusFlag2;

    FelicaStatus(byte statusFlag1, byte statusFlag2) {
        this.statusFlag1 = statusFlag1;
        this.statusFlag2 = statusFlag2;
    }

    /**
     * Gets status flag 1.
     * Refer to the FeliCa card manual for the specific meaning of errors.
     *
     * @return status flag 1
     */
    public byte getStatusFlag1() {
        return statusFlag1;
    }

    /**
     * Gets status flag 2.
     * Refer to the FeliCa card manual for the specific meaning of errors.
     *
     * @return status flag 2
     */
    public byte getStatusFlag2() {
        return statusFlag2;
    }
}
