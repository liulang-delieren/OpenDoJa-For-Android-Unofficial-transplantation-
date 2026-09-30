package com.nttdocomo.ui.ogl.math;

/**
 * Provides float-valued math helpers used by the OpenGL utility API.
 */
public final class FloatMath {
    private FloatMath() {
    }

    /**
     * Returns the sine of the specified angle.
     *
     * @param value the angle in radians
     * @return the sine of the angle
     */
    public static float sin(float value) {
        return (float) java.lang.Math.sin(value);
    }

    /**
     * Returns the cosine of the specified angle.
     *
     * @param value the angle in radians
     * @return the cosine of the angle
     */
    public static float cos(float value) {
        return (float) java.lang.Math.cos(value);
    }

    /**
     * Returns the tangent of the specified angle.
     *
     * @param value the angle in radians
     * @return the tangent of the angle
     */
    public static float tan(float value) {
        return (float) java.lang.Math.tan(value);
    }

    /**
     * Returns the square root of the specified value.
     *
     * @param value the input value
     * @return the square root
     */
    public static float sqrt(float value) {
        return (float) java.lang.Math.sqrt(value);
    }

    /**
     * Returns the arc tangent of the specified value.
     *
     * @param value the input value
     * @return the arc tangent
     */
    public static float atan(float value) {
        return (float) java.lang.Math.atan(value);
    }

    /**
     * Returns the angle from the conversion of rectangular coordinates.
     *
     * @param y the ordinate coordinate
     * @param x the abscissa coordinate
     * @return the theta component
     */
    public static float atan2(float y, float x) {
        return (float) java.lang.Math.atan2(y, x);
    }
}
