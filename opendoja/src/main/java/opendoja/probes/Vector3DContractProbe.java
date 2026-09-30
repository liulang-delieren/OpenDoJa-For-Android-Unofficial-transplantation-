package opendoja.probes;

import com.nttdocomo.ui.util3d.Vector3D;

public final class Vector3DContractProbe {
    private static final float EPSILON = 0.0003f;

    private Vector3DContractProbe() {
    }

    public static void main(String[] args) {
        Vector3D zero = new Vector3D();
        assertVector("default ctor", zero, 0.0f, 0.0f, 0.0f);

        Vector3D seeded = new Vector3D(1.0f, 2.0f, 3.0f);
        Vector3D copy = new Vector3D(seeded);
        assertVector("copy ctor", copy, 1.0f, 2.0f, 3.0f);

        Vector3D setVector = new Vector3D();
        setVector.set(0.25f, -0.5f, 0.75f);
        assertVector("set floats", setVector, 0.25f, -0.5f, 0.75f);
        setVector.set(seeded);
        assertVector("set vector", setVector, 1.0f, 2.0f, 3.0f);

        Vector3D added = new Vector3D(0.1f, 0.2f, 0.3f);
        added.add(0.2f, 0.3f, 0.4f);
        assertVector("add floats", added, 1229.0f / 4096.0f, 2048.0f / 4096.0f, 2867.0f / 4096.0f);

        Vector3D aliasAdd = new Vector3D(0.25f, -0.5f, 0.75f);
        aliasAdd.add(aliasAdd);
        assertVector("add alias", aliasAdd, 0.5f, -1.0f, 1.5f);

        Vector3D axis = new Vector3D(3.0f, 0.0f, 4.0f);
        axis.normalize();
        assertVector("normalize", axis, 0.5998535f, 0.0f, 0.7998047f);

        float dot = new Vector3D(0.5f, 0.25f, -0.75f).dot(new Vector3D(0.5f, -0.5f, 0.25f));
        assertApprox("dot instance", -256.0f / 4096.0f, dot);
        assertApprox("dot static", dot, Vector3D.dot(new Vector3D(0.5f, 0.25f, -0.75f), new Vector3D(0.5f, -0.5f, 0.25f)));

        Vector3D cross = new Vector3D();
        cross.cross(new Vector3D(1.0f, 0.0f, 0.0f), new Vector3D(0.0f, 1.0f, 0.0f));
        assertVector("cross", cross, 0.0f, 0.0f, 1.0f);

        Vector3D selfCross = new Vector3D(1.0f, 2.0f, 3.0f);
        selfCross.cross(selfCross);
        assertVector("cross alias", selfCross, 0.0f, 0.0f, 0.0f);

        Vector3D components = new Vector3D();
        components.setX(1.5f);
        components.setY(-2.5f);
        components.setZ(3.5f);
        assertApprox("getX", 1.5f, components.getX());
        assertApprox("getY", -2.5f, components.getY());
        assertApprox("getZ", 3.5f, components.getZ());

        assertThrows("copy ctor null", NullPointerException.class, () -> new Vector3D((Vector3D) null));
        assertThrows("ctor invalid x", IllegalArgumentException.class, () -> new Vector3D(Float.NaN, 0.0f, 0.0f));
        assertThrows("set invalid", IllegalArgumentException.class, () -> zero.set(Float.POSITIVE_INFINITY, 0.0f, 0.0f));
        assertThrows("set vector null", NullPointerException.class, () -> zero.set((Vector3D) null));
        assertThrows("add invalid", IllegalArgumentException.class, () -> zero.add(Float.NEGATIVE_INFINITY, 0.0f, 0.0f));
        assertThrows("add vector null", NullPointerException.class, () -> zero.add((Vector3D) null));
        assertThrows("setX invalid", IllegalArgumentException.class, () -> zero.setX(Float.NaN));
        assertThrows("setY invalid", IllegalArgumentException.class, () -> zero.setY(Float.POSITIVE_INFINITY));
        assertThrows("setZ invalid", IllegalArgumentException.class, () -> zero.setZ(Float.NEGATIVE_INFINITY));
        assertThrows("normalize zero", ArithmeticException.class, () -> new Vector3D().normalize());
        assertThrows("normalize quantized zero", ArithmeticException.class, () -> new Vector3D(0.00001f, 0.00001f, 0.00001f).normalize());
        assertThrows("dot null", NullPointerException.class, () -> zero.dot(null));
        assertThrows("dot static left null", NullPointerException.class, () -> Vector3D.dot(null, zero));
        assertThrows("dot static right null", NullPointerException.class, () -> Vector3D.dot(zero, null));
        assertThrows("cross null", NullPointerException.class, () -> zero.cross((Vector3D) null));
        assertThrows("cross left null", NullPointerException.class, () -> zero.cross(null, zero));
        assertThrows("cross right null", NullPointerException.class, () -> zero.cross(zero, null));

        System.out.println("Vector3D contract probe OK");
    }

    private static void assertVector(String label, Vector3D vector, float x, float y, float z) {
        assertApprox(label + ".x", x, vector.getX());
        assertApprox(label + ".y", y, vector.getY());
        assertApprox(label + ".z", z, vector.getZ());
    }

    private static void assertApprox(String label, float expected, float actual) {
        if (Math.abs(expected - actual) > EPSILON) {
            throw new IllegalStateException(label + " expected=" + expected + " actual=" + actual);
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
