package com.nttdocomo.device.felica;

/**
 * Defines PIN-enable parameters.
 */
public final class EnablePINParameters extends PINParameters {
    /**
     * Creates an {@code EnablePINParameters} object.
     */
    public EnablePINParameters() {
    }

    /**
     * Registers PIN-enable parameters.
     * Parameters can be registered up to the maximum parameter count returned
     * by {@link OfflineParameters#getMaxSize()}.
     *
     * @param pinType the service code
     * @param pin the PIN
     * @param userChange {@code true} if user-side PIN changes are permitted
     * @return the registered position (index)
     * @throws IllegalArgumentException if {@code pinType} or {@code pin} is negative
     * @throws IllegalStateException if the maximum number of parameters is
     *         already registered
     */
    public int add(int pinType, long pin, boolean userChange) {
        if (pinType < 0 || pin < 0L) {
            throw new IllegalArgumentException();
        }
        return addInternal(new Object[]{Integer.valueOf(pinType), Long.valueOf(pin), Boolean.valueOf(userChange)});
    }
}
