package com.nttdocomo.opt.ui.j3d;

import opendoja.g3d.FixedPoint;

/**
 * Defines a class that handles matrices for three-dimensional affine
 * transforms.
 * By setting an instance of this class with
 * {@link Graphics3D#setViewTrans(AffineTrans)}, object rotation and view
 * transform initialization can be performed.
 *
 * <p>This class does not explicitly define methods for translation or scaling.
 * As alternatives, the screen-transform methods
 * {@link Graphics3D#setScreenCenter(int, int)} and
 * {@link Graphics3D#setScreenScale(int, int)} are intended to be used.</p>
 *
 * <p>Each matrix element is a 32-bit signed fixed-point number that maps
 * {@code 1.0} to {@code 4096}. If a value overflows the {@code int} range
 * during calculation, the result is device-dependent.</p>
 *
 * <p>Introduced in DoJa-2.0.</p>
 */
public class AffineTrans {
    /** The element in row 1, column 1. */
    public int m00;
    /** The element in row 1, column 2. */
    public int m01;
    /** The element in row 1, column 3. */
    public int m02;
    /** The element in row 1, column 4. */
    public int m03;
    /** The element in row 2, column 1. */
    public int m10;
    /** The element in row 2, column 2. */
    public int m11;
    /** The element in row 2, column 3. */
    public int m12;
    /** The element in row 2, column 4. */
    public int m13;
    /** The element in row 3, column 1. */
    public int m20;
    /** The element in row 3, column 2. */
    public int m21;
    /** The element in row 3, column 3. */
    public int m22;
    /** The element in row 3, column 4. */
    public int m23;

    /**
     * Creates an empty matrix.
     */
    public AffineTrans() {
    }

    /**
     * Creates a matrix with the specified elements.
     *
     * @param a00 the element in row 1, column 1
     * @param a01 the element in row 1, column 2
     * @param a02 the element in row 1, column 3
     * @param a03 the element in row 1, column 4
     * @param a10 the element in row 2, column 1
     * @param a11 the element in row 2, column 2
     * @param a12 the element in row 2, column 3
     * @param a13 the element in row 2, column 4
     * @param a20 the element in row 3, column 1
     * @param a21 the element in row 3, column 2
     * @param a22 the element in row 3, column 3
     * @param a23 the element in row 3, column 4
     */
    public AffineTrans(int a00, int a01, int a02, int a03,
                       int a10, int a11, int a12, int a13,
                       int a20, int a21, int a22, int a23) {
        setElement(a00, a01, a02, a03, a10, a11, a12, a13, a20, a21, a22, a23);
    }

    /**
     * Sets the specified element of this matrix.
     *
     * @param row the zero-based row number of the element to set
     * @param column the zero-based column number of the element to set
     * @param value the value to set at row {@code row + 1}, column
     *              {@code column + 1}
     * @throws ArrayIndexOutOfBoundsException if {@code row} or
     *         {@code column} is out of range
     */
    public void setElement(int row, int column, int value) {
        if (row < 0 || row > 2 || column < 0 || column > 3) {
            throw new ArrayIndexOutOfBoundsException();
        }
        switch (row * 4 + column) {
            case 0 -> m00 = value;
            case 1 -> m01 = value;
            case 2 -> m02 = value;
            case 3 -> m03 = value;
            case 4 -> m10 = value;
            case 5 -> m11 = value;
            case 6 -> m12 = value;
            case 7 -> m13 = value;
            case 8 -> m20 = value;
            case 9 -> m21 = value;
            case 10 -> m22 = value;
            case 11 -> m23 = value;
            default -> throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Sets all elements of this matrix.
     *
     * @param a00 the element in row 1, column 1
     * @param a01 the element in row 1, column 2
     * @param a02 the element in row 1, column 3
     * @param a03 the element in row 1, column 4
     * @param a10 the element in row 2, column 1
     * @param a11 the element in row 2, column 2
     * @param a12 the element in row 2, column 3
     * @param a13 the element in row 2, column 4
     * @param a20 the element in row 3, column 1
     * @param a21 the element in row 3, column 2
     * @param a22 the element in row 3, column 3
     * @param a23 the element in row 3, column 4
     */
    public void setElement(int a00, int a01, int a02, int a03,
                           int a10, int a11, int a12, int a13,
                           int a20, int a21, int a22, int a23) {
        this.m00 = a00;
        this.m01 = a01;
        this.m02 = a02;
        this.m03 = a03;
        this.m10 = a10;
        this.m11 = a11;
        this.m12 = a12;
        this.m13 = a13;
        this.m20 = a20;
        this.m21 = a21;
        this.m22 = a22;
        this.m23 = a23;
    }

    /**
     * Sets the elements of the specified row in this matrix.
     *
     * @param row the zero-based row number whose elements are to be set
     * @param x the value for column 1 of the specified row
     * @param y the value for column 2 of the specified row
     * @param z the value for column 3 of the specified row
     * @param w the value for column 4 of the specified row
     * @throws ArrayIndexOutOfBoundsException if {@code row} is outside
     *         {@code 0} through {@code 2}
     */
    public void setRow(int row, int x, int y, int z, int w) {
        switch (row) {
            case 0 -> setElement(x, y, z, w, m10, m11, m12, m13, m20, m21, m22, m23);
            case 1 -> setElement(m00, m01, m02, m03, x, y, z, w, m20, m21, m22, m23);
            case 2 -> setElement(m00, m01, m02, m03, m10, m11, m12, m13, x, y, z, w);
            default -> throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Sets the elements of the specified column in this matrix.
     *
     * @param column the zero-based column number whose elements are to be set
     * @param x the value for row 1 of the specified column
     * @param y the value for row 2 of the specified column
     * @param z the value for row 3 of the specified column
     * @throws ArrayIndexOutOfBoundsException if {@code column} is outside
     *         {@code 0} through {@code 3}
     */
    public void setColumn(int column, int x, int y, int z) {
        switch (column) {
            case 0 -> {
                m00 = x;
                m10 = y;
                m20 = z;
            }
            case 1 -> {
                m01 = x;
                m11 = y;
                m21 = z;
            }
            case 2 -> {
                m02 = x;
                m12 = y;
                m22 = z;
            }
            case 3 -> {
                m03 = x;
                m13 = y;
                m23 = z;
            }
            default -> throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Sets the matrix elements so that the transform becomes the identity
     * transform.
     * This is the same as calling
     * {@code setElement(4096, 0, 0, 0, 0, 4096, 0, 0, 0, 0, 4096, 0)}.
     */
    public void setIdentity() {
        m00 = FixedPoint.ONE;
        m01 = 0;
        m02 = 0;
        m03 = 0;
        m10 = 0;
        m11 = FixedPoint.ONE;
        m12 = 0;
        m13 = 0;
        m20 = 0;
        m21 = 0;
        m22 = FixedPoint.ONE;
        m23 = 0;
    }

    /**
     * Calculates the product of the transform matrices
     * {@code (this x other)} and stores the result in this object.
     * This is the same as calling {@link #mul(AffineTrans, AffineTrans)} with
     * this object and {@code other}. The calculation is correct even when this
     * and {@code other} refer to the same object.
     *
     * @param t the multiplier matrix
     * @throws NullPointerException if {@code t} is {@code null}
     */
    public void mul(AffineTrans t) {
        mul(this, t);
    }

    /**
     * Calculates the product of the transform matrices
     * {@code (left x right)} and stores the result in this object.
     *
     * @param t1 the multiplicand matrix
     * @param t2 the multiplier matrix
     * @throws NullPointerException if {@code t1} or {@code t2} is
     *         {@code null}
     */
    public void mul(AffineTrans t1, AffineTrans t2) {
        if (t1 == null || t2 == null) {
            throw new NullPointerException();
        }
        int a00 = FixedPoint.mul(t1.m00, t2.m00) + FixedPoint.mul(t1.m01, t2.m10) + FixedPoint.mul(t1.m02, t2.m20);
        int a01 = FixedPoint.mul(t1.m00, t2.m01) + FixedPoint.mul(t1.m01, t2.m11) + FixedPoint.mul(t1.m02, t2.m21);
        int a02 = FixedPoint.mul(t1.m00, t2.m02) + FixedPoint.mul(t1.m01, t2.m12) + FixedPoint.mul(t1.m02, t2.m22);
        int a03 = FixedPoint.mul(t1.m00, t2.m03) + FixedPoint.mul(t1.m01, t2.m13) + FixedPoint.mul(t1.m02, t2.m23) + t1.m03;
        int a10 = FixedPoint.mul(t1.m10, t2.m00) + FixedPoint.mul(t1.m11, t2.m10) + FixedPoint.mul(t1.m12, t2.m20);
        int a11 = FixedPoint.mul(t1.m10, t2.m01) + FixedPoint.mul(t1.m11, t2.m11) + FixedPoint.mul(t1.m12, t2.m21);
        int a12 = FixedPoint.mul(t1.m10, t2.m02) + FixedPoint.mul(t1.m11, t2.m12) + FixedPoint.mul(t1.m12, t2.m22);
        int a13 = FixedPoint.mul(t1.m10, t2.m03) + FixedPoint.mul(t1.m11, t2.m13) + FixedPoint.mul(t1.m12, t2.m23) + t1.m13;
        int a20 = FixedPoint.mul(t1.m20, t2.m00) + FixedPoint.mul(t1.m21, t2.m10) + FixedPoint.mul(t1.m22, t2.m20);
        int a21 = FixedPoint.mul(t1.m20, t2.m01) + FixedPoint.mul(t1.m21, t2.m11) + FixedPoint.mul(t1.m22, t2.m21);
        int a22 = FixedPoint.mul(t1.m20, t2.m02) + FixedPoint.mul(t1.m21, t2.m12) + FixedPoint.mul(t1.m22, t2.m22);
        int a23 = FixedPoint.mul(t1.m20, t2.m03) + FixedPoint.mul(t1.m21, t2.m13) + FixedPoint.mul(t1.m22, t2.m23) + t1.m23;
        setElement(a00, a01, a02, a03, a10, a11, a12, a13, a20, a21, a22, a23);
    }

    /**
     * Sets the matrix elements so that this matrix represents rotation around
     * the x-axis in a right-handed coordinate system.
     * The fourth column is not changed.
     *
     * @param a the angle in 4096-per-circle units
     */
    public void setRotateX(int a) {
        int cos = Math.cos(a);
        int sin = Math.sin(a);
        int a03 = m03;
        int a13 = m13;
        int a23 = m23;
        setElement(FixedPoint.ONE, 0, 0, a03, 0, cos, -sin, a13, 0, sin, cos, a23);
    }

    /**
     * Sets the matrix elements so that this matrix represents rotation around
     * the y-axis in a right-handed coordinate system.
     * The fourth column is not changed.
     *
     * @param a the angle in 4096-per-circle units
     */
    public void setRotateY(int a) {
        int cos = Math.cos(a);
        int sin = Math.sin(a);
        int a03 = m03;
        int a13 = m13;
        int a23 = m23;
        setElement(cos, 0, sin, a03, 0, FixedPoint.ONE, 0, a13, -sin, 0, cos, a23);
    }

    /**
     * Sets the matrix elements so that this matrix represents rotation around
     * the z-axis in a right-handed coordinate system.
     * The fourth column is not changed.
     *
     * @param a the angle in 4096-per-circle units
     */
    public void setRotateZ(int a) {
        int cos = Math.cos(a);
        int sin = Math.sin(a);
        int a03 = m03;
        int a13 = m13;
        int a23 = m23;
        setElement(cos, -sin, 0, a03, sin, cos, 0, a13, 0, 0, FixedPoint.ONE, a23);
    }

    /**
     * Sets the matrix elements so that this matrix represents rotation around
     * the specified vector in a right-handed coordinate system.
     * The vector does not need to be a unit vector. The fourth column is not
     * changed.
     *
     * @param v the vector that becomes the axis of rotation
     * @param a the angle in 4096-per-circle units
     * @throws NullPointerException if {@code v} is {@code null}
     */
    public void setRotateV(Vector3D v, int a) {
        if (v == null) {
            throw new NullPointerException();
        }
        Vector3D axis = new Vector3D(v.x, v.y, v.z);
        axis.normalize();
        int cos = Math.cos(a);
        int sin = Math.sin(a);
        int nc = FixedPoint.ONE - cos;
        int x = axis.x;
        int y = axis.y;
        int z = axis.z;
        int a03 = m03;
        int a13 = m13;
        int a23 = m23;
        setElement(
                cos + FixedPoint.mul(FixedPoint.mul(x, x), nc), FixedPoint.mul(FixedPoint.mul(x, y), nc) - FixedPoint.mul(z, sin), FixedPoint.mul(FixedPoint.mul(x, z), nc) + FixedPoint.mul(y, sin), a03,
                FixedPoint.mul(FixedPoint.mul(y, x), nc) + FixedPoint.mul(z, sin), cos + FixedPoint.mul(FixedPoint.mul(y, y), nc), FixedPoint.mul(FixedPoint.mul(y, z), nc) - FixedPoint.mul(x, sin), a13,
                FixedPoint.mul(FixedPoint.mul(z, x), nc) - FixedPoint.mul(y, sin), FixedPoint.mul(FixedPoint.mul(z, y), nc) + FixedPoint.mul(x, sin), cos + FixedPoint.mul(FixedPoint.mul(z, z), nc), a23
        );
    }

    /**
     * Sets the matrix elements so that this matrix represents conversion to
     * view coordinates.
     *
     * @param position the position vector of the viewpoint
     * @param look the position vector of the reference point
     * @param up the up vector
     * @throws NullPointerException if an argument is {@code null}
     */
    public void lookAt(Vector3D position, Vector3D look, Vector3D up) {
        if (position == null || look == null || up == null) {
            throw new NullPointerException();
        }
        if (up.x == 0 && up.y == 0 && up.z == 0) {
            throw new IllegalArgumentException();
        }
        int fx = look.x - position.x;
        int fy = look.y - position.y;
        int fz = look.z - position.z;
        if (fx == 0 && fy == 0 && fz == 0) {
            throw new IllegalArgumentException();
        }
        if (fy * up.z - fz * up.y == 0
                && fz * up.x - fx * up.z == 0
                && fx * up.y - fy * up.x == 0) {
            throw new IllegalArgumentException();
        }
        Vector3D forward = new Vector3D(fx, fy, fz);
        try {
            forward.normalize();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException();
        }
        Vector3D side = new Vector3D();
        side.cross(forward, up);
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
        setElement(
                side.x, side.y, side.z, -viewTranslation(side, position),
                actualUp.x, actualUp.y, actualUp.z, -viewTranslation(actualUp, position),
                forward.x, forward.y, forward.z, -viewTranslation(forward, position)
        );
    }

    /**
     * Transforms the vector that represents point coordinates by this matrix
     * and stores the result into another vector.
     * The calculation is correct even when {@code source} and
     * {@code destination} refer to the same object.
     *
     * @param v the vector that represents point coordinates
     * @param result the vector into which the transformed result is stored
     * @throws NullPointerException if {@code v} or {@code result} is
     *         {@code null}
     */
    public void transform(Vector3D v, Vector3D result) {
        if (v == null || result == null) {
            throw new NullPointerException();
        }
        int x = v.x;
        int y = v.y;
        int z = v.z;
        result.x = FixedPoint.mul(x, m00) + FixedPoint.mul(y, m01) + FixedPoint.mul(z, m02) + m03;
        result.y = FixedPoint.mul(x, m10) + FixedPoint.mul(y, m11) + FixedPoint.mul(z, m12) + m13;
        result.z = FixedPoint.mul(x, m20) + FixedPoint.mul(y, m21) + FixedPoint.mul(z, m22) + m23;
    }

    float[] toFloatMatrix() {
        return new float[]{
                m00 / 4096f, m01 / 4096f, m02 / 4096f, m03,
                m10 / 4096f, m11 / 4096f, m12 / 4096f, m13,
                m20 / 4096f, m21 / 4096f, m22 / 4096f, m23,
                0f, 0f, 0f, 1f
        };
    }

    private static int viewTranslation(Vector3D basis, Vector3D point) {
        return FixedPoint.mul(basis.x, point.x)
                + FixedPoint.mul(basis.y, point.y)
                + FixedPoint.mul(basis.z, point.z);
    }
}
