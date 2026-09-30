package com.nttdocomo.device.felica;

/**
 * Defines PIN-change parameters.
 */
public final class ChangePINParameters extends PINParameters {
    /**
     * Creates a {@code ChangePINParameters} object.
     */
    public ChangePINParameters() {
    }

    /**
     * Registers PIN-change parameters.
     * Parameters can be registered up to the maximum parameter count returned
     * by {@link OfflineParameters#getMaxSize()}.
     *
     * @param pinType the service code
     * @param oldPin the old PIN
     * @param newPin the new PIN
     * @return the registered position (index)
     * @throws IllegalArgumentException if any argument is negative
     * @throws IllegalStateException if the maximum number of parameters is
     *         already registered
     */
    public int add(int pinType, long oldPin, long newPin) {
        if (pinType < 0 || oldPin < 0L || newPin < 0L) {
            throw new IllegalArgumentException();
        }
        return addInternal(new long[]{pinType, oldPin, newPin});
    }
}
