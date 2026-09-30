package com.nttdocomo.ui.util3d;

import opendoja.g3d.FixedPoint;

/**
 * Defines utility methods for fast numeric operations on {@code float}
 * values.
 */
public class FastMath {
    private static final float DEGREES_TO_INTERNAL_ANGLE = 2048.0f / 180.0f;

    /**
     * Applications cannot create this utility class directly.
     */
    protected FastMath() {
    }

    /**
     * Converts a {@code float} into the internal {@code int} representation
     * used by the engine.
     *
     * @param v the float value to convert
     * @return the corresponding internal integer value
     */
    public static int floatToInnerInt(float v) {
        requireFinite(v);
        return FixedPoint.fromFloat(v);
    }

    /**
     * Converts an internal engine integer value back to {@code float}.
     *
     * @param v the internal integer value
     * @return the corresponding float value
     */
    public static float innerIntToFloat(int v) {
        return FixedPoint.toFloat(v);
    }

    /**
     * Calculates the approximate sum of two values.
     *
     * @param x the left operand
     * @param y the right operand
     * @return the approximate sum
     */
    public static float add(float x, float y) {
        requireFinite(x, y);
        return FixedPoint.toFloat(floatToInnerInt(x) + floatToInnerInt(y));
    }

    /**
     * Calculates the approximate difference of two values.
     *
     * @param x the left operand
     * @param y the right operand
     * @return the approximate difference
     */
    public static float sub(float x, float y) {
        return add(x, -y);
    }

    /**
     * Calculates the approximate product of two values.
     *
     * @param x the left operand
     * @param y the right operand
     * @return the approximate product
     */
    public static float mul(float x, float y) {
        requireFinite(x, y);
        return FixedPoint.toFloat(FixedPoint.mul(floatToInnerInt(x), floatToInnerInt(y)));
    }

    /**
     * Calculates the approximate quotient of two values.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the approximate quotient
     */
    public static float div(float x, float y) {
        requireFinite(x, y);
        int dividend = floatToInnerInt(x);
        int divisor = floatToInnerInt(y);
        if (divisor == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return FixedPoint.toFloat((int) ((((long) dividend) << 12) / divisor));
    }

    /**
     * Calculates an approximate square root.
     *
     * @param x the input value
     * @return the approximate square root
     */
    public static float sqrt(float x) {
        requireFinite(x);
        if (x < 0.0f) {
            throw new ArithmeticException("Negative sqrt");
        }
        int inner = floatToInnerInt(x);
        if (inner < 0) {
            throw new ArithmeticException("Negative sqrt");
        }
        long scaled = (long) inner * (long) FixedPoint.ONE;
        return FixedPoint.toFloat((int) java.lang.Math.round(java.lang.Math.sqrt(scaled)));
    }

    /**
     * Calculates an approximate sine value.
     *
     * @param a the angle in degrees
     * @return the approximate sine
     */
    public static float sin(float a) {
        requireFinite(a);
        return FixedPoint.toFloat(FixedPoint.sin(toInternalAngle(a)));
    }

    /**
     * Calculates an approximate cosine value.
     *
     * @param a the angle in degrees
     * @return the approximate cosine
     */
    public static float cos(float a) {
        requireFinite(a);
        return FixedPoint.toFloat(FixedPoint.cos(toInternalAngle(a)));
    }

    /**
     * Calculates an approximate tangent value.
     *
     * @param a the angle in degrees
     * @return the approximate tangent
     */
    public static float tan(float a) {
        requireFinite(a);
        if (isTangentSingularity(a)) {
            return java.lang.Math.sin(java.lang.Math.toRadians(a)) >= 0.0d
                    ? Float.POSITIVE_INFINITY
                    : Float.NEGATIVE_INFINITY;
        }
        int angle = toInternalAngle(a);
        int sine = FixedPoint.sin(angle);
        int cosine = FixedPoint.cos(angle);
        if (cosine == 0) {
            return (float) java.lang.Math.tan(java.lang.Math.toRadians(a));
        }
        return FixedPoint.toFloat((int) ((((long) sine) << 12) / cosine));
    }

    /**
     * Calculates an approximate arcsine value in degrees.
     *
     * @param a the input value
     * @return the approximate arcsine in degrees
     */
    public static float asin(float a) {
        requireFinite(a);
        if (a < -1.0f || a > 1.0f) {
            throw new ArithmeticException("asin domain");
        }
        return (float) java.lang.Math.toDegrees(java.lang.Math.asin(a));
    }

    /**
     * Calculates an approximate arccosine value in degrees.
     *
     * @param a the input value
     * @return the approximate arccosine in degrees
     */
    public static float acos(float a) {
        requireFinite(a);
        if (a < -1.0f || a > 1.0f) {
            throw new ArithmeticException("acos domain");
        }
        return (float) java.lang.Math.toDegrees(java.lang.Math.acos(a));
    }

    /**
     * Calculates an approximate arctangent value in degrees.
     *
     * @param a the input value
     * @return the approximate arctangent in degrees
     */
    public static float atan(float a) {
        requireFinite(a);
        return (float) java.lang.Math.toDegrees(java.lang.Math.atan(a));
    }

    /**
     * Calculates an approximate two-argument arctangent value in degrees.
     *
     * @param a the x coordinate
     * @param b the y coordinate
     * @return the approximate angle in degrees
     */
    public static float atan2(float a, float b) {
        requireFinite(a, b);
        if (a == 0.0f && b == 0.0f) {
            return Float.NaN;
        }
        if (a == 0.0f) {
            return 90.0f;
        }
        if (b == 0.0f) {
            return 0.0f;
        }
        float angle = (float) java.lang.Math.toDegrees(java.lang.Math.atan(b / a));
        return angle < 0.0f ? angle + 180.0f : angle;
    }

    /**
     * Calculates an approximate absolute value.
     *
     * @param a the input value
     * @return the approximate absolute value
     */
    public static float abs(float a) {
        requireFinite(a);
        return java.lang.Math.abs(a);
    }

    private static int toInternalAngle(float degrees) {
        return java.lang.Math.round(degrees * DEGREES_TO_INTERNAL_ANGLE);
    }

    private static boolean isTangentSingularity(float a) {
        float remainder = a % 180.0f;
        return remainder == 90.0f || remainder == -90.0f;
    }

    private static void requireFinite(float... values) {
        for (float value : values) {
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                throw new IllegalArgumentException("Invalid float value");
            }
        }
    }
}
