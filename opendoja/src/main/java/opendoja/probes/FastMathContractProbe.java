package opendoja.probes;

import com.nttdocomo.ui.util3d.FastMath;

public final class FastMathContractProbe {
    private static final float EPSILON = 0.0003f;

    private FastMathContractProbe() {
    }

    public static void main(String[] args) {
        assertEquals("floatToInnerInt", 410, FastMath.floatToInnerInt(0.1f));
        assertApprox("innerIntToFloat", 410.0f / 4096.0f, FastMath.innerIntToFloat(410));

        assertApprox("add", 1229.0f / 4096.0f, FastMath.add(0.1f, 0.2f));
        assertApprox("sub", -409.0f / 4096.0f, FastMath.sub(0.1f, 0.2f));
        assertApprox("mul", 82.0f / 4096.0f, FastMath.mul(0.1f, 0.2f));
        assertApprox("div", 8192.0f / 4096.0f, FastMath.div(1.0f, 0.5f));
        assertApprox("sqrt", 5793.0f / 4096.0f, FastMath.sqrt(2.0f));

        assertApprox("sin", 2046.0f / 4096.0f, FastMath.sin(30.0f));
        assertApprox("cos", 2046.0f / 4096.0f, FastMath.cos(60.0f));
        assertInfinity("tan+inf", FastMath.tan(90.0f), true);
        assertInfinity("tan-inf", FastMath.tan(270.0f), false);
        assertFinite("tan-near-singularity", FastMath.tan(89.99f));
        assertApprox("asin", 30.0f, FastMath.asin(0.5f));
        assertApprox("acos", 60.0f, FastMath.acos(0.5f));
        assertApprox("atan", 45.0f, FastMath.atan(1.0f));
        assertApprox("abs", 1.25f, FastMath.abs(-1.25f));

        assertApprox("atan2-x-axis", 0.0f, FastMath.atan2(10.0f, 0.0f));
        assertApprox("atan2-y-axis-positive", 90.0f, FastMath.atan2(0.0f, 10.0f));
        assertApprox("atan2-y-axis-negative", 90.0f, FastMath.atan2(0.0f, -10.0f));
        assertApprox("atan2-negative-ratio", 135.0f, FastMath.atan2(10.0f, -10.0f));
        assertApprox("atan2-positive-ratio", 45.0f, FastMath.atan2(-10.0f, -10.0f));
        assertNaN("atan2-origin", FastMath.atan2(0.0f, 0.0f));

        assertThrows("floatToInnerInt NaN", IllegalArgumentException.class,
                () -> FastMath.floatToInnerInt(Float.NaN));
        assertThrows("floatToInnerInt +inf", IllegalArgumentException.class,
                () -> FastMath.floatToInnerInt(Float.POSITIVE_INFINITY));
        assertThrows("add invalid", IllegalArgumentException.class,
                () -> FastMath.add(Float.NaN, 1.0f));
        assertThrows("sub invalid", IllegalArgumentException.class,
                () -> FastMath.sub(1.0f, Float.NEGATIVE_INFINITY));
        assertThrows("mul invalid", IllegalArgumentException.class,
                () -> FastMath.mul(Float.POSITIVE_INFINITY, 2.0f));
        assertThrows("div invalid", IllegalArgumentException.class,
                () -> FastMath.div(Float.NaN, 1.0f));
        assertThrows("div zero", ArithmeticException.class,
                () -> FastMath.div(1.0f, 0.0f));
        assertThrows("div underflowed denominator", ArithmeticException.class,
                () -> FastMath.div(1.0f, 0.00001f));
        assertThrows("sqrt invalid", IllegalArgumentException.class,
                () -> FastMath.sqrt(Float.POSITIVE_INFINITY));
        assertThrows("sqrt negative", ArithmeticException.class,
                () -> FastMath.sqrt(-1.0f));
        assertThrows("sin invalid", IllegalArgumentException.class,
                () -> FastMath.sin(Float.NaN));
        assertThrows("cos invalid", IllegalArgumentException.class,
                () -> FastMath.cos(Float.NEGATIVE_INFINITY));
        assertThrows("tan invalid", IllegalArgumentException.class,
                () -> FastMath.tan(Float.POSITIVE_INFINITY));
        assertThrows("asin invalid", IllegalArgumentException.class,
                () -> FastMath.asin(Float.NaN));
        assertThrows("asin domain", ArithmeticException.class,
                () -> FastMath.asin(1.0001f));
        assertThrows("acos invalid", IllegalArgumentException.class,
                () -> FastMath.acos(Float.NEGATIVE_INFINITY));
        assertThrows("acos domain", ArithmeticException.class,
                () -> FastMath.acos(-1.0001f));
        assertThrows("atan invalid", IllegalArgumentException.class,
                () -> FastMath.atan(Float.POSITIVE_INFINITY));
        assertThrows("atan2 invalid-a", IllegalArgumentException.class,
                () -> FastMath.atan2(Float.NaN, 0.0f));
        assertThrows("atan2 invalid-b", IllegalArgumentException.class,
                () -> FastMath.atan2(0.0f, Float.POSITIVE_INFINITY));
        assertThrows("abs invalid", IllegalArgumentException.class,
                () -> FastMath.abs(Float.NaN));

        System.out.println("FastMath contract probe OK");
    }

    private static void assertApprox(String label, float expected, float actual) {
        if (Math.abs(expected - actual) > EPSILON) {
            throw new IllegalStateException(label + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertEquals(String label, int expected, int actual) {
        if (expected != actual) {
            throw new IllegalStateException(label + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertInfinity(String label, float value, boolean positive) {
        if (positive) {
            if (value != Float.POSITIVE_INFINITY) {
                throw new IllegalStateException(label + " expected=+inf actual=" + value);
            }
            return;
        }
        if (value != Float.NEGATIVE_INFINITY) {
            throw new IllegalStateException(label + " expected=-inf actual=" + value);
        }
    }

    private static void assertNaN(String label, float value) {
        if (!Float.isNaN(value)) {
            throw new IllegalStateException(label + " expected=NaN actual=" + value);
        }
    }

    private static void assertFinite(String label, float value) {
        if (!Float.isFinite(value)) {
            throw new IllegalStateException(label + " expected finite actual=" + value);
        }
    }

    private static void assertThrows(String label, Class<? extends Throwable> expected, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (expected.isInstance(throwable)) {
                return;
            }
            throw new IllegalStateException(label + " expected=" + expected.getName()
                    + " actual=" + throwable.getClass().getName(), throwable);
        }
        throw new IllegalStateException(label + " expected exception " + expected.getName());
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
