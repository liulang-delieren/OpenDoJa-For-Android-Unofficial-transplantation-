package opendoja.probes;

import com.nttdocomo.ui.util3d.Transform;
import com.nttdocomo.ui.util3d.Vector3D;

public final class TransformContractProbe {
    private static final float EPSILON = 0.001f;

    private TransformContractProbe() {
    }

    public static void main(String[] args) {
        Transform identity = new Transform();
        assertMatrixElement("default[0]", identity, 0, 1.0f);
        assertMatrixElement("default[5]", identity, 5, 1.0f);
        assertMatrixElement("default[10]", identity, 10, 1.0f);
        assertMatrixElement("default[15]", identity, 15, 1.0f);

        Transform copied = new Transform(identity);
        assertMatrixElement("copy ctor", copied, 0, 1.0f);

        Transform fromArray = new Transform();
        float[] seeded = new float[17];
        for (int i = 0; i < 16; i++) {
            seeded[i] = i + 0.25f;
        }
        seeded[16] = Float.NaN;
        fromArray.set(seeded);
        assertMatrixElement("set array copies first 16", fromArray, 0, 0.25f);
        assertMatrixElement("set array ignores extra", fromArray, 15, 15.25f);

        float[] copyOut = new float[17];
        copyOut[16] = 123.0f;
        fromArray.get(copyOut);
        assertApprox("get array leaves tail", 123.0f, copyOut[16]);

        Transform exact = new Transform();
        exact.set(0, 0.1f);
        if (exact.get(0) != 0.1f) {
            throw new IllegalStateException("set/get exact retention failed");
        }

        Transform transposed = new Transform();
        transposed.set(1, 2.0f);
        transposed.set(4, 3.0f);
        transposed.transpose();
        assertMatrixElement("transpose[1]", transposed, 1, 3.0f);
        assertMatrixElement("transpose[4]", transposed, 4, 2.0f);

        Transform invertible = new Transform();
        invertible.translate(1.0f, 2.0f, 3.0f);
        invertible.invert();
        Vector3D invertedPoint = new Vector3D();
        invertible.transVector(new Vector3D(2.0f, 4.0f, 6.0f), invertedPoint);
        assertVector("invert translate", invertedPoint, 1.0f, 2.0f, 3.0f);

        Transform multiplyAlias = new Transform();
        multiplyAlias.translate(1.0f, 2.0f, 3.0f);
        multiplyAlias.multiply(multiplyAlias);
        assertMatrixElement("multiply alias tx", multiplyAlias, 3, 2.0f);
        assertMatrixElement("multiply alias ty", multiplyAlias, 7, 4.0f);
        assertMatrixElement("multiply alias tz", multiplyAlias, 11, 6.0f);

        Transform scaled = new Transform();
        scaled.scale(2.0f, 3.0f, 4.0f);
        Vector3D scaledPoint = new Vector3D();
        scaled.transVector(new Vector3D(1.0f, 1.0f, 1.0f), scaledPoint);
        assertVector("scale", scaledPoint, 2.0f, 3.0f, 4.0f);

        Transform rotated = new Transform();
        rotated.rotate(0.0f, 0.0f, 1.0f, 90.0f);
        Vector3D rotatedPoint = new Vector3D();
        rotated.transVector(new Vector3D(1.0f, 0.0f, 0.0f), rotatedPoint);
        assertVector("rotate degrees", rotatedPoint, 0.0f, 1.0f, 0.0f);

        Transform rotatedQuat = new Transform();
        rotatedQuat.rotateQuat(0.0f, 1.0f, 0.0f, 0.0f);
        Vector3D rotatedQuatPoint = new Vector3D();
        rotatedQuat.transVector(new Vector3D(1.0f, 2.0f, 3.0f), rotatedQuatPoint);
        assertVector("rotateQuat", rotatedQuatPoint, -1.0f, 2.0f, -3.0f);

        Transform translated = new Transform();
        translated.translate(new Vector3D(1.0f, -2.0f, 3.0f));
        Vector3D alias = new Vector3D(4.0f, 5.0f, 6.0f);
        translated.transVector(alias, alias);
        assertVector("transVector alias", alias, 5.0f, 3.0f, 9.0f);

        Transform lookedAt = new Transform();
        lookedAt.set(12, 7.0f);
        lookedAt.set(13, 8.0f);
        lookedAt.set(14, 9.0f);
        lookedAt.set(15, 10.0f);
        lookedAt.lookAt(
                new Vector3D(1.0f, 0.0f, 0.0f),
                new Vector3D(1.0f, 0.0f, 1.0f),
                new Vector3D(0.0f, 1.0f, 0.0f));
        assertMatrixElement("lookAt preserves row4[12]", lookedAt, 12, 7.0f);
        assertMatrixElement("lookAt preserves row4[13]", lookedAt, 13, 8.0f);
        assertMatrixElement("lookAt preserves row4[14]", lookedAt, 14, 9.0f);
        assertMatrixElement("lookAt preserves row4[15]", lookedAt, 15, 10.0f);
        assertMatrixElement("lookAt tx", lookedAt, 3, 1.0f);
        assertMatrixElement("lookAt ty", lookedAt, 7, 0.0f);
        assertMatrixElement("lookAt tz", lookedAt, 11, 0.0f);

        assertThrows("copy ctor null", NullPointerException.class, () -> new Transform((Transform) null));
        assertThrows("set transform null", NullPointerException.class, () -> identity.set((Transform) null));
        assertThrows("set array null", NullPointerException.class, () -> identity.set((float[]) null));
        assertThrows("set array short", IllegalArgumentException.class, () -> identity.set(new float[15]));
        assertThrows("set array invalid head", IllegalArgumentException.class, () -> identity.set(new float[]{
                Float.NaN, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        }));
        assertThrows("get array null", NullPointerException.class, () -> identity.get((float[]) null));
        assertThrows("get array short", IllegalArgumentException.class, () -> identity.get(new float[15]));
        assertThrows("set index low", IllegalArgumentException.class, () -> identity.set(-1, 0.0f));
        assertThrows("set index high", IllegalArgumentException.class, () -> identity.set(16, 0.0f));
        assertThrows("set invalid value", IllegalArgumentException.class, () -> identity.set(0, Float.POSITIVE_INFINITY));
        assertThrows("get index low", IllegalArgumentException.class, () -> identity.get(-1));
        assertThrows("get index high", IllegalArgumentException.class, () -> identity.get(16));
        assertThrows("invert singular", ArithmeticException.class, () -> {
            Transform singular = new Transform();
            singular.set(new float[16]);
            singular.invert();
        });
        assertThrows("multiply null", NullPointerException.class, () -> identity.multiply(null));
        assertThrows("scale invalid", IllegalArgumentException.class, () -> identity.scale(Float.NaN, 1.0f, 1.0f));
        assertThrows("scale null", NullPointerException.class, () -> identity.scale((Vector3D) null));
        assertThrows("rotate invalid", IllegalArgumentException.class, () -> identity.rotate(Float.NaN, 0.0f, 0.0f, 0.0f));
        assertThrows("rotate quantized zero axis", IllegalArgumentException.class, () -> identity.rotate(0.00001f, 0.00001f, 0.00001f, 30.0f));
        assertThrows("rotate null vector", NullPointerException.class, () -> identity.rotate((Vector3D) null, 30.0f));
        assertThrows("rotateQuat invalid", IllegalArgumentException.class, () -> identity.rotateQuat(0.0f, 0.0f, Float.NEGATIVE_INFINITY, 0.0f));
        assertThrows("rotateQuat zero", IllegalArgumentException.class, () -> identity.rotateQuat(0.00001f, 0.00001f, 0.00001f, 0.00001f));
        assertThrows("rotateQuat null vector", NullPointerException.class, () -> identity.rotateQuat((Vector3D) null, 0.0f));
        assertThrows("rotateQuat invalid w", IllegalArgumentException.class, () -> identity.rotateQuat(new Vector3D(), Float.NaN));
        assertThrows("translate invalid", IllegalArgumentException.class, () -> identity.translate(1.0f, Float.POSITIVE_INFINITY, 1.0f));
        assertThrows("translate null", NullPointerException.class, () -> identity.translate((Vector3D) null));
        assertThrows("lookAt null position", NullPointerException.class,
                () -> identity.lookAt(null, new Vector3D(), new Vector3D(0.0f, 1.0f, 0.0f)));
        assertThrows("lookAt zero up", IllegalArgumentException.class,
                () -> identity.lookAt(new Vector3D(), new Vector3D(0.0f, 0.0f, 1.0f), new Vector3D()));
        assertThrows("lookAt equal points", IllegalArgumentException.class,
                () -> identity.lookAt(new Vector3D(1.0f, 2.0f, 3.0f), new Vector3D(1.0f, 2.0f, 3.0f), new Vector3D(0.0f, 1.0f, 0.0f)));
        assertThrows("lookAt parallel", IllegalArgumentException.class,
                () -> identity.lookAt(new Vector3D(), new Vector3D(0.0f, 0.0f, 1.0f), new Vector3D(0.0f, 0.0f, 2.0f)));
        assertThrows("transVector null source", NullPointerException.class, () -> identity.transVector(null, new Vector3D()));
        assertThrows("transVector null result", NullPointerException.class, () -> identity.transVector(new Vector3D(), null));

        System.out.println("Transform contract probe OK");
    }

    private static void assertVector(String label, Vector3D vector, float x, float y, float z) {
        assertApprox(label + ".x", x, vector.getX());
        assertApprox(label + ".y", y, vector.getY());
        assertApprox(label + ".z", z, vector.getZ());
    }

    private static void assertMatrixElement(String label, Transform transform, int index, float expected) {
        assertApprox(label, expected, transform.get(index));
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
