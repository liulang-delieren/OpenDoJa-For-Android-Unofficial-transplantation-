package com.nttdocomo.device.felica;

/**
 * Defines PIN-check parameters.
 */
public final class CheckPINParameters extends OfflineParameters {
    /**
     * Creates a {@code CheckPINParameters} object.
     */
    public CheckPINParameters() {
    }

    /**
     * Registers PIN-check parameters.
     * Parameters can be registered up to the maximum parameter count returned
     * by {@link #getMaxSize()}.
     *
     * @param pinType the service code
     * @return the registered position (index)
     * @throws IllegalArgumentException if {@code pinType} is negative
     * @throws IllegalStateException if the maximum number of parameters is
     *         already registered
     */
    public int add(int pinType) {
        if (pinType < 0) {
            throw new IllegalArgumentException("pinType");
        }
        return addInternal(Integer.valueOf(pinType));
    }
}
