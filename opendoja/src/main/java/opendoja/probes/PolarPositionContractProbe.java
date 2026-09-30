package opendoja.probes;

import com.nttdocomo.ui.sound3d.PolarPosition;
import com.nttdocomo.ui.util3d.Vector3D;

/**
 * Headless checks for the documented PolarPosition API contract.
 */
public final class PolarPositionContractProbe {
    private PolarPositionContractProbe() {
    }

    public static void main(String[] args) {
        try {
            verifyCoordinateFactor();
            verifyDistance();
            verifyDirection();
            verifyElevation();
            verifyPosition();
            verifyVelocity();
        } finally {
            PolarPosition.setDefaultCoordinateFactor(1f);
        }

        System.out.println("polar-position-contract-ok");
    }

    private static void verifyCoordinateFactor() {
        PolarPosition defaultPosition = new PolarPosition();
        assertFloatEquals(1f, defaultPosition.getCoordinateFactor(), "initial default coordinate factor");
        assertFloatEquals(0f, defaultPosition.getDistance(), "initial distance");
        assertFloatEquals(0f, defaultPosition.getDirection(), "initial direction");
        assertFloatEquals(0f, defaultPosition.getElevation(), "initial elevation");
        assertNull(defaultPosition.getVelocity(), "initial velocity");

        PolarPosition explicit = new PolarPosition(2.5f);
        assertFloatEquals(2.5f, explicit.getCoordinateFactor(), "explicit coordinate factor");

        PolarPosition.setDefaultCoordinateFactor(3.5f);
        assertFloatEquals(3.5f, new PolarPosition().getCoordinateFactor(), "updated default coordinate factor");

        expectIllegalArgument(() -> new PolarPosition(0f), "zero coordinate factor");
        expectIllegalArgument(() -> new PolarPosition(-1f), "negative coordinate factor");
        expectIllegalArgument(() -> new PolarPosition(Float.NaN), "NaN coordinate factor");
        expectIllegalArgument(() -> new PolarPosition(Float.POSITIVE_INFINITY), "positive infinite coordinate factor");
        expectIllegalArgument(() -> new PolarPosition(Float.NEGATIVE_INFINITY), "negative infinite coordinate factor");
        expectIllegalArgument(() -> PolarPosition.setDefaultCoordinateFactor(0f), "zero default coordinate factor");
        expectIllegalArgument(() -> PolarPosition.setDefaultCoordinateFactor(-1f), "negative default coordinate factor");
        expectIllegalArgument(() -> PolarPosition.setDefaultCoordinateFactor(Float.NaN), "NaN default coordinate factor");
        expectIllegalArgument(() -> PolarPosition.setDefaultCoordinateFactor(Float.POSITIVE_INFINITY),
                "positive infinite default coordinate factor");
        expectIllegalArgument(() -> PolarPosition.setDefaultCoordinateFactor(Float.NEGATIVE_INFINITY),
                "negative infinite default coordinate factor");
    }

    private static void verifyDistance() {
        PolarPosition position = new PolarPosition();
        position.setDistance(4.25f);
        assertFloatEquals(4.25f, position.getDistance(), "distance");
        position.setDistance(0f);
        assertFloatEquals(0f, position.getDistance(), "zero distance");
        position.setDistance(Float.POSITIVE_INFINITY);
        assertFloatEquals(Float.POSITIVE_INFINITY, position.getDistance(), "positive infinite distance");

        expectIllegalArgument(() -> position.setDistance(-0.25f), "negative distance");
        expectIllegalArgument(() -> position.setDistance(Float.NaN), "NaN distance");
    }

    private static void verifyDirection() {
        PolarPosition position = new PolarPosition();
        position.setDirection(-1.25f);
        assertFloatEquals(-1.25f, position.getDirection(), "direction");
        expectIllegalArgument(() -> position.setDirection(Float.NaN), "NaN direction");
        expectIllegalArgument(() -> position.setDirection(Float.POSITIVE_INFINITY), "positive infinite direction");
        expectIllegalArgument(() -> position.setDirection(Float.NEGATIVE_INFINITY), "negative infinite direction");
    }

    private static void verifyElevation() {
        PolarPosition position = new PolarPosition();
        position.setElevation(1.25f);
        assertFloatEquals(1.25f, position.getElevation(), "elevation");
        expectIllegalArgument(() -> position.setElevation(Float.NaN), "NaN elevation");
        expectIllegalArgument(() -> position.setElevation(Float.POSITIVE_INFINITY), "positive infinite elevation");
        expectIllegalArgument(() -> position.setElevation(Float.NEGATIVE_INFINITY), "negative infinite elevation");
    }

    private static void verifyPosition() {
        PolarPosition position = new PolarPosition();
        position.setPosition(new Vector3D(2f, 0.5f, -0.25f));
        assertFloatEquals(2f, position.getDistance(), "position distance");
        assertFloatEquals(0.5f, position.getDirection(), "position direction");
        assertFloatEquals(-0.25f, position.getElevation(), "position elevation");

        expectNullPointer(() -> position.setPosition(null), "null position vector");
        expectIllegalArgument(() -> position.setPosition(new Vector3D(-1f, 0f, 0f)), "negative position distance");
    }

    private static void verifyVelocity() {
        PolarPosition position = new PolarPosition();
        assertNull(position.getVelocity(), "default null velocity");
        position.setVelocity(null);
        assertNull(position.getVelocity(), "explicit null velocity");

        Vector3D source = new Vector3D(1f, 2f, 3f);
        position.setVelocity(source);
        source.setX(9f);

        Vector3D velocity = position.getVelocity();
        assertVectorEquals(1f, 2f, 3f, velocity, "stored velocity copy");
        velocity.setY(8f);
        assertVectorEquals(1f, 2f, 3f, position.getVelocity(), "returned velocity copy");

        position.setVelocity(null);
        assertNull(position.getVelocity(), "cleared velocity");
    }

    private static void expectIllegalArgument(ThrowingRunnable runnable, String label) {
        try {
            runnable.run();
            throw new AssertionError(label + " did not throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        } catch (Throwable other) {
            throw new AssertionError(label + " threw wrong exception", other);
        }
    }

    private static void expectNullPointer(ThrowingRunnable runnable, String label) {
        try {
            runnable.run();
            throw new AssertionError(label + " did not throw NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        } catch (Throwable other) {
            throw new AssertionError(label + " threw wrong exception", other);
        }
    }

    private static void assertVectorEquals(float x, float y, float z, Vector3D actual, String label) {
        assertFloatEquals(x, actual.getX(), label + " x");
        assertFloatEquals(y, actual.getY(), label + " y");
        assertFloatEquals(z, actual.getZ(), label + " z");
    }

    private static void assertFloatEquals(float expected, float actual, String label) {
        if (Float.floatToIntBits(expected) != Float.floatToIntBits(actual)) {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertNull(Object value, String label) {
        if (value != null) {
            throw new AssertionError(label + ": expected null");
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
