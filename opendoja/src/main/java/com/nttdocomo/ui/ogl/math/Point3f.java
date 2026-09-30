package com.nttdocomo.ui.ogl.math;

/**
 * Defines a 3D point with float components.
 */
public final class Point3f extends Tuple3f {
    /**
     * Creates a point initialized to zero.
     */
    public Point3f() {
    }

    /**
     * Creates a point with the specified values.
     *
     * @param x the x component
     * @param y the y component
     * @param z the z component
     */
    public Point3f(float x, float y, float z) {
        super(x, y, z);
    }
}
