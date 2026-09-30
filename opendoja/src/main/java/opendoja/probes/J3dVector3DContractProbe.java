package opendoja.probes;

import com.nttdocomo.opt.ui.j3d.Vector3D;

public final class J3dVector3DContractProbe {
    private J3dVector3DContractProbe() {
    }

    public static void main(String[] args) {
        Vector3D zero = new Vector3D();
        assertVector("default ctor", zero, 0, 0, 0);

        Vector3D seeded = new Vector3D(3, 4, 0);
        assertVector("seeded ctor", seeded, 3, 4, 0);

        Vector3D normalized = new Vector3D(3 * 4096, 4 * 4096, 0);
        normalized.normalize();
        assertVector("normalize", normalized, 2457, 3276, 0);

        Vector3D left = new Vector3D(1, 2, 3);
        Vector3D right = new Vector3D(4, 5, 6);
        assertEquals("dot instance", 32, left.dot(right));
        assertEquals("dot static", 32, Vector3D.dot(left, right));

        Vector3D cross = new Vector3D();
        cross.cross(new Vector3D(1, 0, 0), new Vector3D(0, 1, 0));
        assertVector("cross", cross, 0, 0, 1);

        Vector3D selfCross = new Vector3D(1, 2, 3);
        selfCross.cross(selfCross);
        assertVector("cross alias", selfCross, 0, 0, 0);

        assertThrows("normalize zero", ArithmeticException.class, () -> new Vector3D().normalize());
        assertThrows("dot null", NullPointerException.class, () -> left.dot(null));
        assertThrows("dot static left null", NullPointerException.class, () -> Vector3D.dot(null, right));
        assertThrows("dot static right null", NullPointerException.class, () -> Vector3D.dot(left, null));
        assertThrows("cross null", NullPointerException.class, () -> left.cross((Vector3D) null));
        assertThrows("cross left null", NullPointerException.class, () -> left.cross(null, right));
        assertThrows("cross right null", NullPointerException.class, () -> left.cross(left, null));

        System.out.println("j3d Vector3D contract probe OK");
    }

    private static void assertVector(String label, Vector3D vector, int x, int y, int z) {
        assertEquals(label + ".x", x, vector.x);
        assertEquals(label + ".y", y, vector.y);
        assertEquals(label + ".z", z, vector.z);
    }

    private static void assertEquals(String label, int expected, int actual) {
        if (expected != actual) {
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
