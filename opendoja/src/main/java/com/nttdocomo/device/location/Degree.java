package com.nttdocomo.device.location;

/**
 * Represents an angle such as latitude, longitude, or azimuth.
 * The value can be obtained in DEGREE units (floating-point or fixed-point)
 * and in DMS units.
 * In all cases, the internal value is kept in DEGREE units using a
 * floating-point representation, and is converted to fixed-point DEGREE units
 * or DMS units as necessary.
 * Note that precision may be reduced when converting from floating-point DEGREE
 * units to fixed-point DEGREE units or DMS units.
 */
public class Degree {
    private static final double MIN_DEGREE = -512.0d;
    private static final double MAX_DEGREE_EXCLUSIVE = 512.0d;
    private static final long MIN_FIXED_POINT = -(1L << 40);
    private static final long MAX_FIXED_POINT = (1L << 40) - 1;
    private static final double FIXED_POINT_SCALE = 2147483648.0d;

    private final double degrees;

    /**
     * Creates this object by specifying an angle in DMS units.
     * Negative angles are specified by making the {@code degree} argument
     * negative.
     *
     * @param degree the degree part in DMS units
     * @param minute the minute part in DMS units
     * @param centisecond the second part multiplied by 100 in DMS units
     * @throws IllegalArgumentException if an argument is outside its permitted
     *         range or if the represented angle is outside
     *         {@code [-512 degrees 0 minutes 0.00 seconds, 511 degrees 59 minutes 59.99 seconds]}
     * @deprecated In DoJa-4.0LE and DoJa-5.0 (903i) or later, this constructor
     *         was replaced by {@link #Degree(int, int, float)} because it can
     *         handle the second part only to the second decimal place.
     */
    @Deprecated
    public Degree(int degree, int minute, int centisecond) {
        validateDegreePart(degree);
        validateMinute(minute);
        if (centisecond < 0 || centisecond >= 6000) {
            throw new IllegalArgumentException("centisecond out of range: " + centisecond);
        }
        double second = centisecond / 100.0d;
        double value = composeDegrees(degree, minute, second);
        validateDegreeValue(value, "degree");
        this.degrees = value;
    }

    /**
     * Creates this object by specifying an angle in DMS units.
     * Negative angles are specified by making the {@code degree} argument
     * negative.
     * If the {@code second} argument contains a value below the fourth decimal
     * place, it is rounded at the fourth decimal place.
     *
     * @param degree the degree part in DMS units
     * @param minute the minute part in DMS units
     * @param second the second part in DMS units
     * @throws IllegalArgumentException if an argument is outside its permitted
     *         range or if the represented angle is outside
     *         {@code [-512.0 degrees, 512.0 degrees)}
     */
    public Degree(int degree, int minute, float second) {
        validateDegreePart(degree);
        validateMinute(minute);
        if (Float.isNaN(second) || second < 0.0f || second >= 60.0f) {
            throw new IllegalArgumentException("second out of range: " + second);
        }
        double roundedSecond = Math.round(second * 1000.0d) / 1000.0d;
        double value = composeDegrees(degree, minute, roundedSecond);
        validateDegreeValue(value, "degree");
        this.degrees = value;
    }

    /**
     * Creates this object by specifying an angle in fixed-point DEGREE units.
     * The returned integer uses {@code 2^-31} degrees as 1.
     *
     * @param degree the angle as a fixed-point DEGREE value
     * @throws IllegalArgumentException if {@code degree} is outside the
     *         permitted 41-bit range
     */
    public Degree(long degree) {
        if (degree < MIN_FIXED_POINT || degree > MAX_FIXED_POINT) {
            throw new IllegalArgumentException("degree out of range: " + degree);
        }
        this.degrees = degree / FIXED_POINT_SCALE;
    }

    /**
     * Creates this object by specifying an angle in floating-point DEGREE units.
     *
     * @param degree the angle in floating-point DEGREE units
     * @throws IllegalArgumentException if {@code degree} is outside
     *         {@code [-512.0, 512.0)} or is {@code NaN}
     */
    public Degree(double degree) {
        validateDegreeValue(degree, "degree");
        this.degrees = degree;
    }

    /**
     * Gets the degree part when this angle is represented in DMS units.
     * No normalization of the value range is performed.
     *
     * @return the degree part when represented in DMS units
     */
    public int getDegreePart() {
        return (int) (degrees < 0.0d ? Math.ceil(degrees) : Math.floor(degrees));
    }

    /**
     * Gets the minute part when this angle is represented in DMS units.
     *
     * @return the minute part when represented in DMS units
     */
    public int getMinutePart() {
        double absolute = Math.abs(degrees);
        int degreePart = (int) Math.floor(absolute);
        double minutes = (absolute - degreePart) * 60.0d;
        return (int) Math.floor(minutes + 1.0e-10d);
    }

    /**
     * Gets the second part multiplied by 100 when this angle is represented in
     * DMS units.
     *
     * @return the second part multiplied by 100 when represented in DMS units
     * @deprecated In DoJa-4.0LE and DoJa-5.0 (903i) or later, this method was
     *         replaced by {@link #getSecondPart()} because it can handle the
     *         second part only to the second decimal place.
     */
    @Deprecated
    public int getCentisecondPart() {
        return (int) Math.round(getRawSecondPart() * 100.0d);
    }

    /**
     * Gets the second part when this angle is represented in DMS units.
     *
     * @return the second part when represented in DMS units
     */
    public float getSecondPart() {
        return (float) getRawSecondPart();
    }

    /**
     * Gets this angle in fixed-point DEGREE units.
     * The lower 31 bits represent the fractional part, and {@code 2^-31}
     * degrees is treated as 1.
     *
     * @return the value in fixed-point DEGREE units
     */
    public long getFixedPointNumber() {
        long fixed = Math.round(degrees * FIXED_POINT_SCALE);
        if (fixed < MIN_FIXED_POINT) {
            return MIN_FIXED_POINT;
        }
        if (fixed > MAX_FIXED_POINT) {
            return MAX_FIXED_POINT;
        }
        return fixed;
    }

    /**
     * Gets this angle in floating-point DEGREE units.
     *
     * @return the value in floating-point DEGREE units
     */
    public double getFloatingPointNumber() {
        return degrees;
    }

    private static void validateDegreePart(int degree) {
        if (degree < -512 || degree >= 512) {
            throw new IllegalArgumentException("degree part out of range: " + degree);
        }
    }

    private static void validateMinute(int minute) {
        if (minute < 0 || minute >= 60) {
            throw new IllegalArgumentException("minute out of range: " + minute);
        }
    }

    private static void validateDegreeValue(double degree, String argumentName) {
        if (Double.isNaN(degree) || degree < MIN_DEGREE || degree >= MAX_DEGREE_EXCLUSIVE) {
            throw new IllegalArgumentException(argumentName + " out of range: " + degree);
        }
    }

    private static double composeDegrees(int degree, int minute, double second) {
        double absolute = Math.abs(degree) + (minute / 60.0d) + (second / 3600.0d);
        return degree < 0 ? -absolute : absolute;
    }

    private double getRawSecondPart() {
        double absolute = Math.abs(degrees);
        int degreePart = (int) Math.floor(absolute);
        double minutes = (absolute - degreePart) * 60.0d;
        int minutePart = (int) Math.floor(minutes + 1.0e-10d);
        double seconds = (minutes - minutePart) * 60.0d;
        if (seconds < 0.0d && seconds > -1.0e-9d) {
            return 0.0d;
        }
        return seconds;
    }
}
