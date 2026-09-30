package com.nttdocomo.opt.ui;

/**
 * Defines the optional stereoscopic display mode toggle.
 *
 * <p>openDoJa currently models this as a process-local flag. Titles that use
 * {@code StereoScreen} typically render both eye views themselves and only
 * rely on this API to decide whether stereo presentation is active.</p>
 */
public final class StereoScreen {
    private static volatile boolean stereoMode;

    private StereoScreen() {
    }

    /**
     * Enables or disables stereo mode.
     *
     * @param enabled {@code true} to enable stereo mode
     */
    public static void setStereoMode(boolean enabled) {
        stereoMode = enabled;
    }

    /**
     * Returns whether stereo mode is currently enabled.
     *
     * @return {@code true} when stereo mode is enabled
     */
    public static boolean isStereoMode() {
        return stereoMode;
    }

    /**
     * Resets runtime defaults for a new application launch.
     */
    public static void resetRuntimeDefaults() {
        stereoMode = false;
    }
}
