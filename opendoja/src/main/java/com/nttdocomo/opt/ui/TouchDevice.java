package com.nttdocomo.opt.ui;

/**
 * Represents the optional touch device.
 */
public class TouchDevice {
    private static final boolean AVAILABLE = PointingDevice.isAvailable();
    private static boolean enabled;

    /**
     * Applications cannot create this object directly.
     */
    protected TouchDevice() {
    }

    /**
     * Enables or disables the touch device.
     *
     * @param b {@code true} to enable it
     */
    public static void setEnabled(boolean b) {
        enabled = b;
        if (b) {
            PointingDevice.setMode(PointingDevice.MODE_MOUSE);
        }
    }

    /**
     * Returns whether the touch device is enabled.
     *
     * @return {@code true} if enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the current x coordinate.
     *
     * @return the x coordinate, or {@code -1} if unavailable
     */
    public static int getX() {
        return AVAILABLE && enabled ? PointingDevice.getX() : -1;
    }

    /**
     * Returns the current y coordinate.
     *
     * @return the y coordinate, or {@code -1} if unavailable
     */
    public static int getY() {
        return AVAILABLE && enabled ? PointingDevice.getY() : -1;
    }

    /**
     * Returns whether the touch device is available.
     *
     * @return {@code true} on the desktop host
     */
    public static boolean isAvailable() {
        return AVAILABLE;
    }
}
