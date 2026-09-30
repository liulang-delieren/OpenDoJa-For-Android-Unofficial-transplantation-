package com.nttdocomo.device.felica;

/**
 * Defines offline read parameters.
 */
public final class ReadParameters extends OfflineParameters {
    /**
     * Creates a {@code ReadParameters} object.
     */
    public ReadParameters() {
    }

    /**
     * Registers read parameters.
     * Parameters can be registered up to the maximum parameter count returned
     * by {@link OfflineParameters#getMaxSize()}.
     *
     * @param serviceCode the service code
     * @param nodeCode the node code
     * @return the registered position (index)
     * @throws IllegalArgumentException if {@code serviceCode} or
     *         {@code nodeCode} is negative
     * @throws IllegalStateException if the maximum number of parameters is
     *         already registered
     */
    public int add(int serviceCode, int nodeCode) {
        if (serviceCode < 0 || nodeCode < 0) {
            throw new IllegalArgumentException();
        }
        return addInternal(new int[]{serviceCode, nodeCode});
    }
}
