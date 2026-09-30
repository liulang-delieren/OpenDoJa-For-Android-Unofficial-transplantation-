package com.nttdocomo.opt.ui.j3d;

import opendoja.g3d.FixedPoint;

/**
 * Defines a utility class for numerical calculations.
 * Depending on the handset, this class may not be supported; in that case an
 * {@link UnsupportedOperationException} occurs when a method is called.
 *
 * <p>Introduced in DoJa-2.0.</p>
 */
public final class Math {
    private Math() {
    }

    /**
     * Calculates an approximate square root.
     * Unlike the other methods in this class, the argument and return value of
     * this method are normal integers, not fixed-point values.
     *
     * @param value the value
     * @return an approximate square root of the specified value
     * @throws ArithmeticException if {@code value} is negative
     */
    public static int sqrt(int value) {
        return FixedPoint.sqrt(value);
    }

    /**
     * Calculates an approximate sine.
     *
     * @param angle the angle in 4096-per-circle units
     *              ({@code 2*pi} radians is {@code 4096}); any signed integer
     *              can be specified
     * @return 4096 times the sine of the specified angle
     */
    public static int sin(int angle) {
        return FixedPoint.sin(angle);
    }

    /**
     * Calculates an approximate cosine.
     *
     * @param angle the angle in 4096-per-circle units
     *              ({@code 2*pi} radians is {@code 4096}); any signed integer
     *              can be specified
     * @return 4096 times the cosine of the specified angle
     */
    public static int cos(int angle) {
        return FixedPoint.cos(angle);
    }

    /**
     * Returns the arctangent of {@code y/x} from the specified values.
     *
     * @param y the fixed-point value stored in an {@code int}
     * @param x the fixed-point value stored in an {@code int}
     * @return the arctangent of {@code y/x} in the range {@code [0, 2*pi)},
     *         converted by multiplying by {@code 4096 / 2*pi}
     */
    public static int atan2(int y, int x) {
        return FixedPoint.atan2(y, x);
    }
}
