package com.nttdocomo.ui.ogl.math;

/**
 * Defines a 3D vector with float components.
 */
public final class Vector3f extends Tuple3f {
    /**
     * Creates a vector initialized to zero.
     */
    public Vector3f() {
    }

    /**
     * Creates a vector with the specified values.
     *
     * @param x the x component
     * @param y the y component
     * @param z the z component
     */
    public Vector3f(float x, float y, float z) {
        super(x, y, z);
    }

    /**
     * Normalizes this vector.
     */
    public void normalize() {
        float len = (float) java.lang.Math.sqrt(x * x + y * y + z * z);
        if (len > 0f) {
            x /= len;
            y /= len;
            z /= len;
        }
    }

    /**
     * Returns the dot product with another vector.
     *
     * @param vector the other vector
     * @return the dot product
     */
    public float dot(Vector3f vector) {
        if (vector == null) {
            throw new NullPointerException("vector");
        }
        return x * vector.x + y * vector.y + z * vector.z;
    }

    /**
     * Sets this vector to the cross product of two vectors.
     *
     * @param left the left vector
     * @param right the right vector
     */
    public void cross(Vector3f left, Vector3f right) {
        if (left == null || right == null) {
            throw new NullPointerException("vector");
        }
        float nextX = left.y * right.z - left.z * right.y;
        float nextY = left.z * right.x - left.x * right.z;
        float nextZ = left.x * right.y - left.y * right.x;
        x = nextX;
        y = nextY;
        z = nextZ;
    }
}
