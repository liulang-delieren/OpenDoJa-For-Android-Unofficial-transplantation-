package com.nttdocomo.device.location;

import com.nttdocomo.lang.IllegalStateException;
import com.nttdocomo.lang.UnsupportedOperationException;

/**
 * Provides electronic-compass functions.
 * The azimuth of the direction faced by the top of the terminal's main screen
 * can be obtained.
 */
public class Compass {
    private static final Compass INSTANCE = new Compass();

    private boolean enabled;

    Compass() {
    }

    /**
     * Returns the electronic-compass instance.
     * If the implementation does not support the electronic-compass function,
     * an exception is thrown.
     *
     * @return the electronic-compass instance
     * @throws UnsupportedOperationException if the electronic-compass function
     *         is not supported
     */
    public static Compass getCompass() {
        if (!_LocationSupport.compassSupported()) {
            throw new UnsupportedOperationException("Compass is not supported by openDoJa");
        }
        return INSTANCE;
    }

    /**
     * Gets the azimuth of the direction faced by this terminal.
     * The angle is measured clockwise from north.
     * This method must be called while the electronic compass is enabled.
     *
     * @return a {@link Degree} object representing the azimuth
     * @throws IllegalStateException if the electronic-compass function is not enabled
     */
    public Degree getAzimuth() {
        if (!enabled) {
            throw new IllegalStateException("Compass is disabled");
        }
        return _LocationSupport.currentAzimuth();
    }

    /**
     * Enables or disables the electronic-compass function.
     *
     * @param enabled {@code true} to enable the electronic compass,
     *        {@code false} to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
