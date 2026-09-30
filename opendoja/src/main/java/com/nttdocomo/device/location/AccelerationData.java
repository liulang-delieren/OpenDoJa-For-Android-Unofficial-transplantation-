package com.nttdocomo.device.location;

import com.nttdocomo.lang.UnsupportedOperationException;

/**
 * Represents acceleration-measurement data.
 */
public class AccelerationData {
    private int[] accelerationX;
    private int[] accelerationY;
    private int[] accelerationZ;
    private int[] roll;
    private int[] pitch;
    private int[] screenOrientation;
    private int size;

    AccelerationData() {
        this(null, null, null, null, null, null);
    }

    AccelerationData(int[] accelerationX, int[] accelerationY, int[] accelerationZ,
                     int[] roll, int[] pitch, int[] screenOrientation) {
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.accelerationZ = accelerationZ;
        this.roll = roll;
        this.pitch = pitch;
        this.screenOrientation = screenOrientation;
        this.size = inferSize();
    }

    /**
     * Gets the X-axis acceleration values.
     *
     * @return the X-axis acceleration values in mg, ordered from older
     *         measurements to newer ones
     * @throws UnsupportedOperationException if the implementation does not
     *         support X-axis acceleration values
     */
    public int[] getAccelerationX() {
        return requireSupported(accelerationX, "X-axis acceleration");
    }

    /**
     * Gets the Y-axis acceleration values.
     *
     * @return the Y-axis acceleration values in mg, ordered from older
     *         measurements to newer ones
     * @throws UnsupportedOperationException if the implementation does not
     *         support Y-axis acceleration values
     */
    public int[] getAccelerationY() {
        return requireSupported(accelerationY, "Y-axis acceleration");
    }

    /**
     * Gets the Z-axis acceleration values.
     *
     * @return the Z-axis acceleration values in mg, ordered from older
     *         measurements to newer ones
     * @throws UnsupportedOperationException if the implementation does not
     *         support Z-axis acceleration values
     */
    public int[] getAccelerationZ() {
        return requireSupported(accelerationZ, "Z-axis acceleration");
    }

    /**
     * Gets the roll-angle values.
     *
     * @return the roll-angle values, ordered from older measurements to newer
     *         ones
     * @throws UnsupportedOperationException if roll-angle acquisition is not
     *         supported
     */
    public int[] getRoll() {
        return requireSupported(roll, "roll");
    }

    /**
     * Gets the pitch-angle values.
     *
     * @return the pitch-angle values, ordered from older measurements to newer
     *         ones
     * @throws UnsupportedOperationException if pitch-angle acquisition is not
     *         supported
     */
    public int[] getPitch() {
        return requireSupported(pitch, "pitch");
    }

    /**
     * Gets the main-screen orientation values.
     *
     * @return the main-screen orientation values, ordered from older
     *         measurements to newer ones
     * @throws UnsupportedOperationException if main-screen orientation
     *         acquisition is not supported
     */
    public int[] getScreenOrientation() {
        return requireSupported(screenOrientation, "screen orientation");
    }

    /**
     * Gets the number of measurement-data items contained in this object.
     *
     * @return the number of measurement-data items, or 0 if no measurement data
     *         is contained
     */
    public int size() {
        return size;
    }

    private static int[] requireSupported(int[] value, String label) {
        if (value == null) {
            throw new UnsupportedOperationException("Acceleration data is not supported: " + label);
        }
        return value;
    }

    void update(
            int size,
            int[] accelerationX,
            int[] accelerationY,
            int[] accelerationZ,
            int[] roll,
            int[] pitch,
            int[] screenOrientation
    ) {
        this.size = Math.max(0, size);
        this.accelerationX = copyInto(this.accelerationX, accelerationX);
        this.accelerationY = copyInto(this.accelerationY, accelerationY);
        this.accelerationZ = copyInto(this.accelerationZ, accelerationZ);
        this.roll = copyInto(this.roll, roll);
        this.pitch = copyInto(this.pitch, pitch);
        this.screenOrientation = copyInto(this.screenOrientation, screenOrientation);
    }

    private static int[] copyInto(int[] current, int[] source) {
        if (source == null) {
            return null;
        }
        if (current == null || current.length < source.length) {
            current = new int[source.length];
        }
        System.arraycopy(source, 0, current, 0, source.length);
        return current;
    }

    private int inferSize() {
        if (accelerationX != null) {
            return accelerationX.length;
        }
        if (accelerationY != null) {
            return accelerationY.length;
        }
        if (accelerationZ != null) {
            return accelerationZ.length;
        }
        if (roll != null) {
            return roll.length;
        }
        if (pitch != null) {
            return pitch.length;
        }
        if (screenOrientation != null) {
            return screenOrientation.length;
        }
        return 0;
    }
}
