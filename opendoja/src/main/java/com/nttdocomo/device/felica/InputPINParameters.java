package com.nttdocomo.device.felica;

/**
 * Defines PIN-input parameters.
 */
public final class InputPINParameters extends PINParameters {
    /**
     * Creates an {@code InputPINParameters} object.
     */
    public InputPINParameters() {
    }

    /**
     * Registers PIN-input parameters.
     * Parameters can be registered up to the maximum parameter count returned
     * by {@link OfflineParameters#getMaxSize()}.
     *
     * @param pinType the service code
     * @param pin the PIN
     * @return the registered position (index)
     * @throws IllegalArgumentException if {@code pinType} or {@code pin} is negative
     * @throws IllegalStateException if the maximum number of parameters is
     *         already registered
     */
    public int add(int pinType, long pin) {
        if (pinType < 0 || pin < 0L) {
            throw new IllegalArgumentException();
        }
        return addInternal(new long[]{pinType, pin});
    }
}
