package com.nttdocomo.device.gesturereader;

/**
 * Represents the recognition result of a roll gesture.
 * This object stores the quality of the recognition result, the amount of
 * movement computed from the latest two consecutive images, and the amount of
 * movement computed from the reference image to the latest image.
 */
public class RollData {
    /** Indicates that the recognition result is reliable (=0). */
    public static final int QUALITY_RELIABLE = 0;

    /** Indicates that the recognition result is unreliable because the acquired image lacked detail (=1). */
    public static final int QUALITY_LOW_DETAIL = 1;

    /** Indicates that the recognition result is unreliable because the subject moved too fast (=2). */
    public static final int QUALITY_TOO_FAST = 2;

    /** Indicates that the same recognition result as the previous request was returned (=3). */
    public static final int QUALITY_SAME_VALUE = 3;

    private int quality = QUALITY_RELIABLE;
    private final float[] immediateMotion = new float[2];
    private final float[] accumulatedMotion = new float[2];

    /**
     * Creates a roll-data object.
     */
    public RollData() {
    }

    /**
     * Gets the quality of the recognition result output by the gesture engine.
     *
     * @return one of {@link #QUALITY_RELIABLE},
     *         {@link #QUALITY_LOW_DETAIL}, {@link #QUALITY_TOO_FAST}, or
     *         {@link #QUALITY_SAME_VALUE}
     */
    public int getQuality() {
        return quality;
    }

    /**
     * Gets the x-direction and y-direction movement amounts computed from the
     * latest two consecutive images.
     * The returned array is the array internally held by this object.
     *
     * @return the immediate-motion array; element 0 is x and element 1 is y
     */
    public float[] getImmediateMotion() {
        return immediateMotion;
    }

    /**
     * Gets the x-direction and y-direction movement amounts from the reference
     * image to the latest image.
     * The returned array is the array internally held by this object.
     *
     * @return the accumulated-motion array; element 0 is x and element 1 is y
     */
    public float[] getAccumulatedMotion() {
        return accumulatedMotion;
    }

    void update(int quality, float immediateX, float immediateY, float accumulatedX, float accumulatedY) {
        this.quality = quality;
        immediateMotion[0] = immediateX;
        immediateMotion[1] = immediateY;
        accumulatedMotion[0] = accumulatedX;
        accumulatedMotion[1] = accumulatedY;
    }
}
