package com.nttdocomo.ui.util3d;

import opendoja.g3d.Software3DContext;

/**
 * Defines a 4x4 transformation matrix for the utility 3D API.
 */
public class Transform {
    private final float[] matrix = Software3DContext.identity();

    /**
     * Creates a transform initialized to the identity matrix.
     */
    public Transform() {
    }

    /**
     * Creates a transform by copying another transform.
     *
     * @param transform the source transform
     * @throws NullPointerException if {@code transform} is {@code null}
     */
    public Transform(Transform transform) {
        set(transform);
    }

    /**
     * Resets this matrix to the identity transform.
     */
    public void setIdentity() {
        System.arraycopy(Software3DContext.identity(), 0, matrix, 0, 16);
    }

    /**
     * Replaces this matrix with a copy of another transform.
     *
     * @param transform the source transform
     * @throws NullPointerException if {@code transform} is {@code null}
     */
    public void set(Transform transform) {
        if (transform == null) {
            throw new NullPointerException();
        }
        System.arraycopy(transform.matrix, 0, matrix, 0, 16);
    }

    /**
     * Replaces this matrix from the first 16 values of the supplied array.
     *
     * @param matrix the source matrix values
     */
    public void set(float[] matrix) {
        if (matrix == null) {
            throw new NullPointerException();
        }
        if (matrix.length < 16) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < 16; i++) {
            requireFinite(matrix[i]);
        }
        System.arraycopy(matrix, 0, this.matrix, 0, 16);
    }

    /**
     * Copies this matrix into the supplied array.
     *
     * @param matrix the destination array
     */
    public void get(float[] matrix) {
        if (matrix == null) {
            throw new NullPointerException();
        }
        if (matrix.length < 16) {
            throw new IllegalArgumentException();
        }
        System.arraycopy(this.matrix, 0, matrix, 0, 16);
    }

    /**
     * Sets one matrix element.
     *
     * @param index the element index from {@code 0} to {@code 15}
     * @param value the new value
     */
    public void set(int index, float value) {
        validateIndex(index);
        requireFinite(value);
        matrix[index] = value;
    }

    /**
     * Gets one matrix element.
     *
     * @param index the element index from {@code 0} to {@code 15}
     * @return the element value
     */
    public float get(int index) {
        validateIndex(index);
        return matrix[index];
    }

    /**
     * Replaces this matrix with its inverse.
     */
    public void invert() {
        float[] inverse = invertMatrix(matrix);
        if (inverse == null) {
            throw new ArithmeticException();
        }
        System.arraycopy(inverse, 0, matrix, 0, 16);
    }

    /**
     * Replaces this matrix with its transpose.
     */
    public void transpose() {
        swap(1, 4);
        swap(2, 8);
        swap(3, 12);
        swap(6, 9);
        swap(7, 13);
        swap(11, 14);
    }

    /**
     * Multiplies this transform by another transform.
     *
     * @param transform the transform multiplied on the right
     * @throws NullPointerException if {@code transform} is {@code null}
     */
    public void multiply(Transform transform) {
        if (transform == null) {
            throw new NullPointerException();
        }
        float[] result = multiplyMatrices(matrix, transform.matrix);
        System.arraycopy(result, 0, matrix, 0, 16);
    }

    /**
     * Applies a scale transform using the supplied factors.
     *
     * @param x the x scale factor
     * @param y the y scale factor
     * @param z the z scale factor
     */
    public void scale(float x, float y, float z) {
        requireFinite(x);
        requireFinite(y);
        requireFinite(z);
        float[] scale = Software3DContext.identity();
        scale[0] = x;
        scale[5] = y;
        scale[10] = z;
        multiply(scale);
    }

    /**
     * Applies a scale transform using the components of a vector.
     *
     * @param v the vector whose components define the scale factors
     * @throws NullPointerException if {@code v} is {@code null}
     */
    public void scale(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        scale(v.getX(), v.getY(), v.getZ());
    }

    /**
     * Applies a rotation around the supplied axis vector.
     *
     * @param x the x component of the axis
     * @param y the y component of the axis
     * @param z the z component of the axis
     * @param angle the rotation angle
     */
    public void rotate(float x, float y, float z, float angle) {
        requireFinite(x);
        requireFinite(y);
        requireFinite(z);
        requireFinite(angle);
        if (angle == 0.0f) {
            return;
        }
        int ix = FastMath.floatToInnerInt(x);
        int iy = FastMath.floatToInnerInt(y);
        int iz = FastMath.floatToInnerInt(z);
        if (ix == 0 && iy == 0 && iz == 0) {
            throw new IllegalArgumentException();
        }
        Vector3D axis = new Vector3D(
                FastMath.innerIntToFloat(ix),
                FastMath.innerIntToFloat(iy),
                FastMath.innerIntToFloat(iz));
        axis.normalize();
        float c = FastMath.cos(angle);
        float s = FastMath.sin(angle);
        float oneMinusC = FastMath.sub(1.0f, c);
        float nx = axis.getX();
        float ny = axis.getY();
        float nz = axis.getZ();
        float[] rotation = Software3DContext.identity();
        rotation[0] = FastMath.add(FastMath.mul(FastMath.mul(nx, nx), oneMinusC), c);
        rotation[1] = FastMath.sub(FastMath.mul(FastMath.mul(nx, ny), oneMinusC), FastMath.mul(nz, s));
        rotation[2] = FastMath.add(FastMath.mul(FastMath.mul(nx, nz), oneMinusC), FastMath.mul(ny, s));
        rotation[4] = FastMath.add(FastMath.mul(FastMath.mul(ny, nx), oneMinusC), FastMath.mul(nz, s));
        rotation[5] = FastMath.add(FastMath.mul(FastMath.mul(ny, ny), oneMinusC), c);
        rotation[6] = FastMath.sub(FastMath.mul(FastMath.mul(ny, nz), oneMinusC), FastMath.mul(nx, s));
        rotation[8] = FastMath.sub(FastMath.mul(FastMath.mul(nx, nz), oneMinusC), FastMath.mul(ny, s));
        rotation[9] = FastMath.add(FastMath.mul(FastMath.mul(ny, nz), oneMinusC), FastMath.mul(nx, s));
        rotation[10] = FastMath.add(FastMath.mul(FastMath.mul(nz, nz), oneMinusC), c);
        multiply(rotation);
    }

    /**
     * Applies a rotation around the supplied axis vector.
     *
     * @param v the axis vector
     * @param angle the rotation angle
     * @throws NullPointerException if {@code v} is {@code null}
     */
    public void rotate(Vector3D v, float angle) {
        if (v == null) {
            throw new NullPointerException();
        }
        requireFinite(angle);
        if (angle != 0.0f && isZeroVector(v.getX(), v.getY(), v.getZ())) {
            throw new IllegalArgumentException();
        }
        rotate(v.getX(), v.getY(), v.getZ(), angle);
    }

    /**
     * Applies a quaternion-based rotation using explicit quaternion
     * components.
     *
     * @param x the quaternion x component
     * @param y the quaternion y component
     * @param z the quaternion z component
     * @param w the quaternion w component
     */
    public void rotateQuat(float x, float y, float z, float w) {
        requireFinite(x);
        requireFinite(y);
        requireFinite(z);
        requireFinite(w);
        if (isZeroQuaternion(x, y, z, w)) {
            throw new IllegalArgumentException();
        }
        float[] normalized = normalizeQuaternion(x, y, z, w);
        float qx = normalized[0];
        float qy = normalized[1];
        float qz = normalized[2];
        float qw = normalized[3];
        float[] rotation = Software3DContext.identity();
        rotation[0] = FastMath.sub(1.0f, FastMath.add(FastMath.mul(2.0f, FastMath.mul(qy, qy)), FastMath.mul(2.0f, FastMath.mul(qz, qz))));
        rotation[1] = FastMath.sub(FastMath.mul(2.0f, FastMath.mul(qx, qy)), FastMath.mul(2.0f, FastMath.mul(qz, qw)));
        rotation[2] = FastMath.add(FastMath.mul(2.0f, FastMath.mul(qx, qz)), FastMath.mul(2.0f, FastMath.mul(qy, qw)));
        rotation[4] = FastMath.add(FastMath.mul(2.0f, FastMath.mul(qx, qy)), FastMath.mul(2.0f, FastMath.mul(qz, qw)));
        rotation[5] = FastMath.sub(1.0f, FastMath.add(FastMath.mul(2.0f, FastMath.mul(qx, qx)), FastMath.mul(2.0f, FastMath.mul(qz, qz))));
        rotation[6] = FastMath.sub(FastMath.mul(2.0f, FastMath.mul(qy, qz)), FastMath.mul(2.0f, FastMath.mul(qx, qw)));
        rotation[8] = FastMath.sub(FastMath.mul(2.0f, FastMath.mul(qx, qz)), FastMath.mul(2.0f, FastMath.mul(qy, qw)));
        rotation[9] = FastMath.add(FastMath.mul(2.0f, FastMath.mul(qy, qz)), FastMath.mul(2.0f, FastMath.mul(qx, qw)));
        rotation[10] = FastMath.sub(1.0f, FastMath.add(FastMath.mul(2.0f, FastMath.mul(qx, qx)), FastMath.mul(2.0f, FastMath.mul(qy, qy))));
        multiply(rotation);
    }

    /**
     * Applies a quaternion-based rotation.
     *
     * @param v the quaternion xyz vector
     * @param w the quaternion w component
     * @throws NullPointerException if {@code v} is {@code null}
     */
    public void rotateQuat(Vector3D v, float w) {
        if (v == null) {
            throw new NullPointerException();
        }
        requireFinite(w);
        if (w == 0.0f && isZeroVector(v.getX(), v.getY(), v.getZ())) {
            throw new IllegalArgumentException();
        }
        rotateQuat(v.getX(), v.getY(), v.getZ(), w);
    }

    /**
     * Applies a translation using the supplied offsets.
     *
     * @param x the x translation
     * @param y the y translation
     * @param z the z translation
     */
    public void translate(float x, float y, float z) {
        requireFinite(x);
        requireFinite(y);
        requireFinite(z);
        float[] translation = Software3DContext.identity();
        translation[3] = x;
        translation[7] = y;
        translation[11] = z;
        multiply(translation);
    }

    /**
     * Applies a translation using the components of a vector.
     *
     * @param v the translation vector
     * @throws NullPointerException if {@code v} is {@code null}
     */
    public void translate(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        translate(v.getX(), v.getY(), v.getZ());
    }

    /**
     * Sets this matrix to the view transform defined by an eye position, a
     * reference point, and an up vector.
     *
     * @param position the eye position
     * @param look the point the camera looks at
     * @param up the up vector
     * @throws NullPointerException if any argument is {@code null}
     */
    public void lookAt(Vector3D position, Vector3D look, Vector3D up) {
        if (position == null || look == null || up == null) {
            throw new NullPointerException();
        }
        int px = FastMath.floatToInnerInt(position.getX());
        int py = FastMath.floatToInnerInt(position.getY());
        int pz = FastMath.floatToInnerInt(position.getZ());
        int lx = FastMath.floatToInnerInt(look.getX());
        int ly = FastMath.floatToInnerInt(look.getY());
        int lz = FastMath.floatToInnerInt(look.getZ());
        int ux = FastMath.floatToInnerInt(up.getX());
        int uy = FastMath.floatToInnerInt(up.getY());
        int uz = FastMath.floatToInnerInt(up.getZ());
        if (ux == 0 && uy == 0 && uz == 0) {
            throw new IllegalArgumentException();
        }
        int fx = lx - px;
        int fy = ly - py;
        int fz = lz - pz;
        if (fx == 0 && fy == 0 && fz == 0) {
            throw new IllegalArgumentException();
        }
        int crossX = fy * uz - fz * uy;
        int crossY = fz * ux - fx * uz;
        int crossZ = fx * uy - fy * ux;
        if (crossX == 0 && crossY == 0 && crossZ == 0) {
            throw new IllegalArgumentException();
        }
        Vector3D forward = new Vector3D(FastMath.innerIntToFloat(fx), FastMath.innerIntToFloat(fy), FastMath.innerIntToFloat(fz));
        forward.normalize();
        Vector3D side = new Vector3D();
        side.cross(forward, new Vector3D(FastMath.innerIntToFloat(ux), FastMath.innerIntToFloat(uy), FastMath.innerIntToFloat(uz)));
        try {
            side.normalize();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException();
        }
        Vector3D actualUp = new Vector3D();
        actualUp.cross(forward, side);
        try {
            actualUp.normalize();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException();
        }
        matrix[0] = side.getX();
        matrix[1] = side.getY();
        matrix[2] = side.getZ();
        matrix[4] = actualUp.getX();
        matrix[5] = actualUp.getY();
        matrix[6] = actualUp.getZ();
        matrix[8] = forward.getX();
        matrix[9] = forward.getY();
        matrix[10] = forward.getZ();
        Vector3D quantizedPosition = new Vector3D(
                FastMath.innerIntToFloat(px),
                FastMath.innerIntToFloat(py),
                FastMath.innerIntToFloat(pz));
        matrix[3] = FastMath.sub(0.0f, side.dot(quantizedPosition));
        matrix[7] = FastMath.sub(0.0f, actualUp.dot(quantizedPosition));
        matrix[11] = FastMath.sub(0.0f, forward.dot(quantizedPosition));
    }

    /**
     * Transforms a point vector by this matrix and stores the result in
     * another vector. The fourth row is ignored and the operation is treated
     * as a 4x3 transform.
     *
     * @param v the source point vector
     * @param result the destination vector
     * @throws NullPointerException if either argument is {@code null}
     */
    public void transVector(Vector3D v, Vector3D result) {
        if (v == null || result == null) {
            throw new NullPointerException();
        }
        float x = v.getX();
        float y = v.getY();
        float z = v.getZ();
        result.set(
                FastMath.add(FastMath.add(FastMath.add(FastMath.mul(matrix[0], x), FastMath.mul(matrix[1], y)), FastMath.mul(matrix[2], z)), matrix[3]),
                FastMath.add(FastMath.add(FastMath.add(FastMath.mul(matrix[4], x), FastMath.mul(matrix[5], y)), FastMath.mul(matrix[6], z)), matrix[7]),
                FastMath.add(FastMath.add(FastMath.add(FastMath.mul(matrix[8], x), FastMath.mul(matrix[9], y)), FastMath.mul(matrix[10], z)), matrix[11])
        );
    }

    float[] raw() {
        return matrix.clone();
    }

    private void multiply(float[] other) {
        float[] result = multiplyMatrices(matrix, other);
        System.arraycopy(result, 0, matrix, 0, 16);
    }

    private void swap(int left, int right) {
        float value = matrix[left];
        matrix[left] = matrix[right];
        matrix[right] = value;
    }

    private static float[] invertMatrix(float[] matrix) {
        float[] inverse = new float[16];
        float det;
        inverse[0] = matrix[5] * matrix[10] * matrix[15] - matrix[5] * matrix[11] * matrix[14] - matrix[9] * matrix[6] * matrix[15] + matrix[9] * matrix[7] * matrix[14] + matrix[13] * matrix[6] * matrix[11] - matrix[13] * matrix[7] * matrix[10];
        inverse[4] = -matrix[4] * matrix[10] * matrix[15] + matrix[4] * matrix[11] * matrix[14] + matrix[8] * matrix[6] * matrix[15] - matrix[8] * matrix[7] * matrix[14] - matrix[12] * matrix[6] * matrix[11] + matrix[12] * matrix[7] * matrix[10];
        inverse[8] = matrix[4] * matrix[9] * matrix[15] - matrix[4] * matrix[11] * matrix[13] - matrix[8] * matrix[5] * matrix[15] + matrix[8] * matrix[7] * matrix[13] + matrix[12] * matrix[5] * matrix[11] - matrix[12] * matrix[7] * matrix[9];
        inverse[12] = -matrix[4] * matrix[9] * matrix[14] + matrix[4] * matrix[10] * matrix[13] + matrix[8] * matrix[5] * matrix[14] - matrix[8] * matrix[6] * matrix[13] - matrix[12] * matrix[5] * matrix[10] + matrix[12] * matrix[6] * matrix[9];
        inverse[1] = -matrix[1] * matrix[10] * matrix[15] + matrix[1] * matrix[11] * matrix[14] + matrix[9] * matrix[2] * matrix[15] - matrix[9] * matrix[3] * matrix[14] - matrix[13] * matrix[2] * matrix[11] + matrix[13] * matrix[3] * matrix[10];
        inverse[5] = matrix[0] * matrix[10] * matrix[15] - matrix[0] * matrix[11] * matrix[14] - matrix[8] * matrix[2] * matrix[15] + matrix[8] * matrix[3] * matrix[14] + matrix[12] * matrix[2] * matrix[11] - matrix[12] * matrix[3] * matrix[10];
        inverse[9] = -matrix[0] * matrix[9] * matrix[15] + matrix[0] * matrix[11] * matrix[13] + matrix[8] * matrix[1] * matrix[15] - matrix[8] * matrix[3] * matrix[13] - matrix[12] * matrix[1] * matrix[11] + matrix[12] * matrix[3] * matrix[9];
        inverse[13] = matrix[0] * matrix[9] * matrix[14] - matrix[0] * matrix[10] * matrix[13] - matrix[8] * matrix[1] * matrix[14] + matrix[8] * matrix[2] * matrix[13] + matrix[12] * matrix[1] * matrix[10] - matrix[12] * matrix[2] * matrix[9];
        inverse[2] = matrix[1] * matrix[6] * matrix[15] - matrix[1] * matrix[7] * matrix[14] - matrix[5] * matrix[2] * matrix[15] + matrix[5] * matrix[3] * matrix[14] + matrix[13] * matrix[2] * matrix[7] - matrix[13] * matrix[3] * matrix[6];
        inverse[6] = -matrix[0] * matrix[6] * matrix[15] + matrix[0] * matrix[7] * matrix[14] + matrix[4] * matrix[2] * matrix[15] - matrix[4] * matrix[3] * matrix[14] - matrix[12] * matrix[2] * matrix[7] + matrix[12] * matrix[3] * matrix[6];
        inverse[10] = matrix[0] * matrix[5] * matrix[15] - matrix[0] * matrix[7] * matrix[13] - matrix[4] * matrix[1] * matrix[15] + matrix[4] * matrix[3] * matrix[13] + matrix[12] * matrix[1] * matrix[7] - matrix[12] * matrix[3] * matrix[5];
        inverse[14] = -matrix[0] * matrix[5] * matrix[14] + matrix[0] * matrix[6] * matrix[13] + matrix[4] * matrix[1] * matrix[14] - matrix[4] * matrix[2] * matrix[13] - matrix[12] * matrix[1] * matrix[6] + matrix[12] * matrix[2] * matrix[5];
        inverse[3] = -matrix[1] * matrix[6] * matrix[11] + matrix[1] * matrix[7] * matrix[10] + matrix[5] * matrix[2] * matrix[11] - matrix[5] * matrix[3] * matrix[10] - matrix[9] * matrix[2] * matrix[7] + matrix[9] * matrix[3] * matrix[6];
        inverse[7] = matrix[0] * matrix[6] * matrix[11] - matrix[0] * matrix[7] * matrix[10] - matrix[4] * matrix[2] * matrix[11] + matrix[4] * matrix[3] * matrix[10] + matrix[8] * matrix[2] * matrix[7] - matrix[8] * matrix[3] * matrix[6];
        inverse[11] = -matrix[0] * matrix[5] * matrix[11] + matrix[0] * matrix[7] * matrix[9] + matrix[4] * matrix[1] * matrix[11] - matrix[4] * matrix[3] * matrix[9] - matrix[8] * matrix[1] * matrix[7] + matrix[8] * matrix[3] * matrix[5];
        inverse[15] = matrix[0] * matrix[5] * matrix[10] - matrix[0] * matrix[6] * matrix[9] - matrix[4] * matrix[1] * matrix[10] + matrix[4] * matrix[2] * matrix[9] + matrix[8] * matrix[1] * matrix[6] - matrix[8] * matrix[2] * matrix[5];
        det = matrix[0] * inverse[0] + matrix[1] * inverse[4] + matrix[2] * inverse[8] + matrix[3] * inverse[12];
        if (det == 0f) {
            return null;
        }
        det = 1f / det;
        for (int i = 0; i < inverse.length; i++) {
            inverse[i] *= det;
        }
        return inverse;
    }

    private static float[] multiplyMatrices(float[] left, float[] right) {
        float[] result = new float[16];
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                int base = row * 4;
                result[base + column] = FastMath.add(
                        FastMath.add(FastMath.mul(left[base], right[column]), FastMath.mul(left[base + 1], right[column + 4])),
                        FastMath.add(FastMath.mul(left[base + 2], right[column + 8]), FastMath.mul(left[base + 3], right[column + 12])));
            }
        }
        return result;
    }

    private static void requireFinite(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException();
        }
    }

    private static void validateIndex(int index) {
        if (index < 0 || index >= 16) {
            throw new IllegalArgumentException();
        }
    }

    private static boolean isZeroVector(float x, float y, float z) {
        return FastMath.floatToInnerInt(x) == 0
                && FastMath.floatToInnerInt(y) == 0
                && FastMath.floatToInnerInt(z) == 0;
    }

    private static boolean isZeroQuaternion(float x, float y, float z, float w) {
        return FastMath.floatToInnerInt(x) == 0
                && FastMath.floatToInnerInt(y) == 0
                && FastMath.floatToInnerInt(z) == 0
                && FastMath.floatToInnerInt(w) == 0;
    }

    private static float[] normalizeQuaternion(float x, float y, float z, float w) {
        int qx = FastMath.floatToInnerInt(x);
        int qy = FastMath.floatToInnerInt(y);
        int qz = FastMath.floatToInnerInt(z);
        int qw = FastMath.floatToInnerInt(w);
        long lengthSquared = (long) qx * (long) qx + (long) qy * (long) qy + (long) qz * (long) qz + (long) qw * (long) qw;
        int length = (int) java.lang.Math.round(java.lang.Math.sqrt(lengthSquared));
        if (length == 0) {
            throw new IllegalArgumentException();
        }
        return new float[]{
                FastMath.innerIntToFloat((int) ((((long) qx) << 12) / length)),
                FastMath.innerIntToFloat((int) ((((long) qy) << 12) / length)),
                FastMath.innerIntToFloat((int) ((((long) qz) << 12) / length)),
                FastMath.innerIntToFloat((int) ((((long) qw) << 12) / length))
        };
    }
}
