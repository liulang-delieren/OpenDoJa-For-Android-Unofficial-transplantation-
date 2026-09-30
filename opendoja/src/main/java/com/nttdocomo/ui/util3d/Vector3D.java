package com.nttdocomo.ui.util3d;

/**
 * Defines a three-dimensional vector made up of {@code float} x, y, and z
 * components.
 */
public class Vector3D {
    private float x;
    private float y;
    private float z;

    /**
     * Creates a vector whose components are all {@code 0}.
     */
    public Vector3D() {
    }

    /**
     * Creates a new vector by copying all components from another vector.
     *
     * @param other the source vector
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public Vector3D(Vector3D v) {
        set(v);
    }

    /**
     * Creates a vector with the specified components.
     *
     * @param x the x component
     * @param y the y component
     * @param z the z component
     */
    public Vector3D(float x, float y, float z) {
        set(x, y, z);
    }

    /**
     * Sets all components of this vector.
     *
     * @param x the x component
     * @param y the y component
     * @param z the z component
     */
    public void set(float x, float y, float z) {
        requireFinite(x);
        requireFinite(y);
        requireFinite(z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Replaces this vector with a full copy of another vector.
     *
     * @param other the source vector
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public void set(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        set(v.x, v.y, v.z);
    }

    /**
     * Adds the specified component values to this vector.
     *
     * @param x the x component to add
     * @param y the y component to add
     * @param z the z component to add
     */
    public void add(float x, float y, float z) {
        requireFinite(x);
        requireFinite(y);
        requireFinite(z);
        set(FastMath.add(getX(), x), FastMath.add(getY(), y), FastMath.add(getZ(), z));
    }

    /**
     * Adds another vector to this vector.
     *
     * @param other the vector to add
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public void add(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        add(v.getX(), v.getY(), v.getZ());
    }

    /**
     * Sets the x component.
     *
     * @param x the x component value
     */
    public void setX(float x) {
        requireFinite(x);
        this.x = x;
    }

    /**
     * Sets the y component.
     *
     * @param y the y component value
     */
    public void setY(float y) {
        requireFinite(y);
        this.y = y;
    }

    /**
     * Sets the z component.
     *
     * @param z the z component value
     */
    public void setZ(float z) {
        requireFinite(z);
        this.z = z;
    }

    /**
     * Gets the x component.
     *
     * @return the x component value
     */
    public float getX() {
        return x;
    }

    /**
     * Gets the y component.
     *
     * @return the y component value
     */
    public float getY() {
        return y;
    }

    /**
     * Gets the z component.
     *
     * @return the z component value
     */
    public float getZ() {
        return z;
    }

    /**
     * Normalizes this vector to unit length.
     */
    public void normalize() {
        int xInt = FastMath.floatToInnerInt(x);
        int yInt = FastMath.floatToInnerInt(y);
        int zInt = FastMath.floatToInnerInt(z);
        if (xInt == 0 && yInt == 0 && zInt == 0) {
            throw new ArithmeticException();
        }
        long lengthSquared = (long) xInt * (long) xInt + (long) yInt * (long) yInt + (long) zInt * (long) zInt;
        int length = (int) java.lang.Math.round(java.lang.Math.sqrt(lengthSquared));
        if (length == 0) {
            throw new ArithmeticException();
        }
        set(
                FastMath.innerIntToFloat((int) ((((long) xInt) << 12) / length)),
                FastMath.innerIntToFloat((int) ((((long) yInt) << 12) / length)),
                FastMath.innerIntToFloat((int) ((((long) zInt) << 12) / length)));
    }

    /**
     * Calculates the dot product of this vector and another vector.
     *
     * @param other the other vector
     * @return the dot-product value
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public float dot(Vector3D v) {
        return dot(this, v);
    }

    /**
     * Calculates the dot product of two vectors.
     *
     * @param left the first vector
     * @param right the second vector
     * @return the dot-product value
     * @throws NullPointerException if either argument is {@code null}
     */
    public static float dot(Vector3D v1, Vector3D v2) {
        if (v1 == null || v2 == null) {
            throw new NullPointerException();
        }
        return FastMath.add(
                FastMath.add(FastMath.mul(v1.getX(), v2.getX()), FastMath.mul(v1.getY(), v2.getY())),
                FastMath.mul(v1.getZ(), v2.getZ()));
    }

    /**
     * Calculates the cross product of this vector and another vector and stores
     * the result in this object.
     *
     * @param other the other vector
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public void cross(Vector3D v) {
        cross(this, v);
    }

    /**
     * Calculates the cross product {@code left x right} and stores the result
     * in this object.
     *
     * @param left the first vector
     * @param right the second vector
     * @throws NullPointerException if either argument is {@code null}
     */
    public void cross(Vector3D u, Vector3D v) {
        if (u == null || v == null) {
            throw new NullPointerException();
        }
        float ux = u.getX();
        float uy = u.getY();
        float uz = u.getZ();
        float vx = v.getX();
        float vy = v.getY();
        float vz = v.getZ();
        float x = FastMath.sub(FastMath.mul(uy, vz), FastMath.mul(uz, vy));
        float y = FastMath.sub(FastMath.mul(uz, vx), FastMath.mul(ux, vz));
        float z = FastMath.sub(FastMath.mul(ux, vy), FastMath.mul(uy, vx));
        set(x, y, z);
    }

    private static void requireFinite(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            throw new IllegalArgumentException();
        }
    }
}
