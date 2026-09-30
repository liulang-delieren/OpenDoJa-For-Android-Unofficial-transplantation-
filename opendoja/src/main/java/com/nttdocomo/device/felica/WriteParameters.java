package com.nttdocomo.device.felica;

/**
 * Defines offline write parameters.
 */
public final class WriteParameters extends OfflineParameters {
    /**
     * Creates a {@code WriteParameters} object.
     */
    public WriteParameters() {
    }

    /**
     * Registers write parameters.
     * Parameters can be registered up to the maximum parameter count returned
     * by {@link OfflineParameters#getMaxSize()}.
     *
     * @param serviceCode the service code
     * @param nodeCode the block number
     * @param data the data to write
     * @return the registered position (index)
     * @throws IllegalArgumentException if {@code serviceCode} or
     *         {@code nodeCode} is negative, or if {@code data} is
     *         {@code null}
     * @throws IllegalStateException if the maximum number of parameters is
     *         already registered
     */
    public int add(int serviceCode, int nodeCode, FelicaData data) {
        if (serviceCode < 0 || nodeCode < 0 || data == null) {
            throw new IllegalArgumentException();
        }
        return addInternal(new Object[]{Integer.valueOf(serviceCode), Integer.valueOf(nodeCode), data});
    }
}
