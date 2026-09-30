package opendoja.probes;

import com.nttdocomo.opt.ui.j3d.AffineTrans;
import com.nttdocomo.opt.ui.j3d.Vector3D;
import opendoja.g3d.FixedPoint;

public final class AffineTransContractProbe {
    private AffineTransContractProbe() {
    }

    public static void main(String[] args) {
        AffineTrans zero = new AffineTrans();
        assertMatrix("default ctor", zero, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        AffineTrans seeded = new AffineTrans(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        assertMatrix("seeded ctor", seeded, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

        seeded.setElement(0, 0, 99);
        seeded.setElement(2, 3, -7);
        assertEquals("setElement row/column m00", 99, seeded.m00);
        assertEquals("setElement row/column m23", -7, seeded.m23);

        seeded.setElement(10, 11, 12, 13, 20, 21, 22, 23, 30, 31, 32, 33);
        assertMatrix("setElement all", seeded, 10, 11, 12, 13, 20, 21, 22, 23, 30, 31, 32, 33);

        seeded.setRow(1, 101, 102, 103, 104);
        assertMatrix("setRow", seeded, 10, 11, 12, 13, 101, 102, 103, 104, 30, 31, 32, 33);

        seeded.setColumn(2, 201, 202, 203);
        assertMatrix("setColumn", seeded, 10, 11, 201, 13, 101, 102, 202, 104, 30, 31, 203, 33);

        AffineTrans identity = new AffineTrans();
        identity.setIdentity();
        assertMatrix("setIdentity", identity,
                4096, 0, 0, 0,
                0, 4096, 0, 0,
                0, 0, 4096, 0);

        AffineTrans left = new AffineTrans(
                4096, 0, 0, 7,
                0, 4096, 0, 8,
                0, 0, 4096, 9);
        AffineTrans right = new AffineTrans(
                4096, 0, 0, 1,
                0, 4096, 0, 2,
                0, 0, 4096, 3);
        left.mul(right);
        assertMatrix("mul instance alias-safe", left,
                4096, 0, 0, 8,
                0, 4096, 0, 10,
                0, 0, 4096, 12);

        AffineTrans product = new AffineTrans();
        product.mul(
                new AffineTrans(4096, 0, 0, 7, 0, 4096, 0, 8, 0, 0, 4096, 9),
                new AffineTrans(4096, 0, 0, 1, 0, 4096, 0, 2, 0, 0, 4096, 3));
        assertMatrix("mul static", product,
                4096, 0, 0, 8,
                0, 4096, 0, 10,
                0, 0, 4096, 12);

        AffineTrans rotateX = new AffineTrans(0, 0, 0, 11, 0, 0, 0, 12, 0, 0, 0, 13);
        rotateX.setRotateX(1024);
        assertEquals("setRotateX keep m03", 11, rotateX.m03);
        assertEquals("setRotateX keep m13", 12, rotateX.m13);
        assertEquals("setRotateX keep m23", 13, rotateX.m23);
        assertEquals("setRotateX m11", 0, rotateX.m11);
        assertEquals("setRotateX m12", -4096, rotateX.m12);
        assertEquals("setRotateX m21", 4096, rotateX.m21);
        assertEquals("setRotateX m22", 0, rotateX.m22);

        AffineTrans rotateY = new AffineTrans(0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 0, 23);
        rotateY.setRotateY(1024);
        assertEquals("setRotateY keep m03", 21, rotateY.m03);
        assertEquals("setRotateY keep m13", 22, rotateY.m13);
        assertEquals("setRotateY keep m23", 23, rotateY.m23);
        assertEquals("setRotateY m00", 0, rotateY.m00);
        assertEquals("setRotateY m02", 4096, rotateY.m02);
        assertEquals("setRotateY m20", -4096, rotateY.m20);
        assertEquals("setRotateY m22", 0, rotateY.m22);

        AffineTrans rotateZ = new AffineTrans(0, 0, 0, 31, 0, 0, 0, 32, 0, 0, 0, 33);
        rotateZ.setRotateZ(1024);
        assertEquals("setRotateZ keep m03", 31, rotateZ.m03);
        assertEquals("setRotateZ keep m13", 32, rotateZ.m13);
        assertEquals("setRotateZ keep m23", 33, rotateZ.m23);
        assertEquals("setRotateZ m00", 0, rotateZ.m00);
        assertEquals("setRotateZ m01", -4096, rotateZ.m01);
        assertEquals("setRotateZ m10", 4096, rotateZ.m10);
        assertEquals("setRotateZ m11", 0, rotateZ.m11);

        AffineTrans rotateV = new AffineTrans(0, 0, 0, 41, 0, 0, 0, 42, 0, 0, 0, 43);
        rotateV.setRotateV(new Vector3D(4096, 0, 0), 1024);
        assertEquals("setRotateV keep m03", 41, rotateV.m03);
        assertEquals("setRotateV keep m13", 42, rotateV.m13);
        assertEquals("setRotateV keep m23", 43, rotateV.m23);
        assertEquals("setRotateV m11", 0, rotateV.m11);
        assertEquals("setRotateV m12", -4096, rotateV.m12);
        assertEquals("setRotateV m21", 4096, rotateV.m21);
        assertEquals("setRotateV m22", 0, rotateV.m22);

        AffineTrans lookedAt = new AffineTrans();
        lookedAt.lookAt(new Vector3D(4096, 0, 0), new Vector3D(4096, 0, 4096), new Vector3D(0, 4096, 0));
        assertMatrix("lookAt orientation", lookedAt,
                -4096, 0, 0, 4096,
                0, -4096, 0, 0,
                0, 0, 4096, 0);

        AffineTrans transform = new AffineTrans();
        transform.setIdentity();
        transform.m03 = 4096;
        transform.m13 = -4096;
        transform.m23 = 8192;
        Vector3D point = new Vector3D(4096, 8192, 12288);
        transform.transform(point, point);
        assertVector("transform alias", point, 8192, 4096, 20480);

        assertThrows("setElement row low", ArrayIndexOutOfBoundsException.class, () -> zero.setElement(-1, 0, 0));
        assertThrows("setElement row high", ArrayIndexOutOfBoundsException.class, () -> zero.setElement(3, 0, 0));
        assertThrows("setElement column low", ArrayIndexOutOfBoundsException.class, () -> zero.setElement(0, -1, 0));
        assertThrows("setElement column high", ArrayIndexOutOfBoundsException.class, () -> zero.setElement(0, 4, 0));
        assertThrows("setRow high", ArrayIndexOutOfBoundsException.class, () -> zero.setRow(3, 0, 0, 0, 0));
        assertThrows("setColumn high", ArrayIndexOutOfBoundsException.class, () -> zero.setColumn(4, 0, 0, 0));
        assertThrows("mul instance null", NullPointerException.class, () -> zero.mul((AffineTrans) null));
        assertThrows("mul left null", NullPointerException.class, () -> zero.mul(null, identity));
        assertThrows("mul right null", NullPointerException.class, () -> zero.mul(identity, null));
        assertThrows("setRotateV null", NullPointerException.class, () -> zero.setRotateV(null, 0));
        assertThrows("lookAt null", NullPointerException.class, () -> zero.lookAt(null, new Vector3D(), new Vector3D(0, 4096, 0)));
        assertThrows("lookAt zero up", IllegalArgumentException.class,
                () -> zero.lookAt(new Vector3D(), new Vector3D(0, 0, 4096), new Vector3D()));
        assertThrows("lookAt equal", IllegalArgumentException.class,
                () -> zero.lookAt(new Vector3D(1, 2, 3), new Vector3D(1, 2, 3), new Vector3D(0, 4096, 0)));
        assertThrows("lookAt parallel", IllegalArgumentException.class,
                () -> zero.lookAt(new Vector3D(), new Vector3D(0, 0, 4096), new Vector3D(0, 0, FixedPoint.ONE)));
        assertThrows("transform null source", NullPointerException.class, () -> zero.transform(null, new Vector3D()));
        assertThrows("transform null result", NullPointerException.class, () -> zero.transform(new Vector3D(), null));

        System.out.println("AffineTrans contract probe OK");
    }

    private static void assertMatrix(String label, AffineTrans actual,
                                     int m00, int m01, int m02, int m03,
                                     int m10, int m11, int m12, int m13,
                                     int m20, int m21, int m22, int m23) {
        assertEquals(label + ".m00", m00, actual.m00);
        assertEquals(label + ".m01", m01, actual.m01);
        assertEquals(label + ".m02", m02, actual.m02);
        assertEquals(label + ".m03", m03, actual.m03);
        assertEquals(label + ".m10", m10, actual.m10);
        assertEquals(label + ".m11", m11, actual.m11);
        assertEquals(label + ".m12", m12, actual.m12);
        assertEquals(label + ".m13", m13, actual.m13);
        assertEquals(label + ".m20", m20, actual.m20);
        assertEquals(label + ".m21", m21, actual.m21);
        assertEquals(label + ".m22", m22, actual.m22);
        assertEquals(label + ".m23", m23, actual.m23);
    }

    private static void assertVector(String label, Vector3D actual, int x, int y, int z) {
        assertEquals(label + ".x", x, actual.x);
        assertEquals(label + ".y", y, actual.y);
        assertEquals(label + ".z", z, actual.z);
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
