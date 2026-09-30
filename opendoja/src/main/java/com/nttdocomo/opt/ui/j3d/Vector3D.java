package com.nttdocomo.opt.ui.j3d;

/**
 * Defines a class that represents a three-dimensional fixed-point vector.
 * Each component is handled as a fixed-point number that maps {@code 1.0} to
 * {@code 4096}. Therefore, the length of a vector after
 * {@link #normalize()} is {@code 4096}.
 *
 * <p>If calculation overflows the {@code int} range, the result is
 * device-dependent.</p>
 *
 * <p>Introduced in DoJa-2.0.</p>
 */
public class Vector3D {
    /**
     * The x component of the vector.
     */
    public int x;

    /**
     * The y component of the vector.
     */
    public int y;

    /**
     * The z component of the vector.
     */
    public int z;

    /**
     * Creates a vector object.
     * All components are initialized to {@code 0}.
     */
    public Vector3D() {
    }

    /**
     * Creates a vector object with the specified components.
     *
     * @param x the x component of the vector
     * @param y the y component of the vector
     * @param z the z component of the vector
     */
    public Vector3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Normalizes this vector.
     * The length of the normalized vector is {@code 4096}.
     *
     */
    public void normalize() {
        long lengthSquared = (long) x * (long) x + (long) y * (long) y + (long) z * (long) z;
        int length = (int) java.lang.Math.round(java.lang.Math.sqrt(lengthSquared));
        if (length == 0) {
            throw new ArithmeticException();
        }
        x = (int) ((((long) x) << 12) / length);
        y = (int) ((((long) y) << 12) / length);
        z = (int) ((((long) z) << 12) / length);
    }

    /**
     * Calculates the inner product of this vector and the specified vector.
     * This is the same as calling {@link #dot(Vector3D, Vector3D)} with this
     * object and {@code v}.
     *
     * @param v the vector with which to take the inner product
     * @return the inner-product value
     * @throws NullPointerException if {@code v} is {@code null}
     */
    public int dot(Vector3D v) {
        return dot(this, v);
    }

    /**
     * Calculates the inner product of the two specified vectors.
     *
     * @param v1 one of the vectors
     * @param v2 the other vector
     * @return the inner-product value of {@code v1} and {@code v2}
     * @throws NullPointerException if {@code v1} or {@code v2} is
     *         {@code null}
     */
    public static int dot(Vector3D v1, Vector3D v2) {
        if (v1 == null || v2 == null) {
            throw new NullPointerException();
        }
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    /**
     * Calculates the cross product of this vector and the specified vector.
     * This is the same as calling {@link #cross(Vector3D, Vector3D)} with this
     * object and {@code v}.
     *
     * @param v the vector with which to take the cross product
     * @throws NullPointerException if {@code v} is {@code null}
     */
    public void cross(Vector3D v) {
        cross(this, v);
    }

    /**
     * Calculates the cross product of the two specified vectors.
     * The result of {@code u x v} is stored in this object.
     *
     * @param u one of the vectors
     * @param v the other vector
     * @throws NullPointerException if {@code u} or {@code v} is
     *         {@code null}
     */
    public void cross(Vector3D u, Vector3D v) {
        if (u == null || v == null) {
            throw new NullPointerException();
        }
        int x = u.y * v.z - u.z * v.y;
        int y = u.z * v.x - u.x * v.z;
        int z = u.x * v.y - u.y * v.x;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
