package com.nttdocomo.device.felica;

/**
 * Holds PIN attribute flags.
 */
public final class PINAttributeData {
    private final boolean pinEnableFlag0;
    private final boolean pinEnableFlag1;
    private final boolean userChangeFlag0;
    private final boolean userChangeFlag1;

    PINAttributeData(boolean pinEnableFlag0, boolean pinEnableFlag1, boolean userChangeFlag0, boolean userChangeFlag1) {
        this.pinEnableFlag0 = pinEnableFlag0;
        this.pinEnableFlag1 = pinEnableFlag1;
        this.userChangeFlag0 = userChangeFlag0;
        this.userChangeFlag1 = userChangeFlag1;
    }

    /**
     * Returns the value of PIN Enable Flag 0.
     *
     * @return {@code true} if the flag value is {@code 1}; {@code false} if it
     *         is {@code 0}
     */
    public boolean getPINEnableFlag0() {
        return pinEnableFlag0;
    }

    /**
     * Returns the value of PIN Enable Flag 1.
     *
     * @return {@code true} if the flag value is {@code 1}; {@code false} if it
     *         is {@code 0}
     */
    public boolean getPINEnableFlag1() {
        return pinEnableFlag1;
    }

    /**
     * Returns the value of User Change Flag 0.
     *
     * @return {@code true} if the flag value is {@code 1}; {@code false} if it
     *         is {@code 0}
     */
    public boolean getUserChangeFlag0() {
        return userChangeFlag0;
    }

    /**
     * Returns the value of User Change Flag 1.
     *
     * @return {@code true} if the flag value is {@code 1}; {@code false} if it
     *         is {@code 0}
     */
    public boolean getUserChangeFlag1() {
        return userChangeFlag1;
    }
}
