package com.nttdocomo.ui.ogl.math;

/**
 * Defines a 4D point with float components.
 */
public final class Point4f extends Tuple4f {
    /**
     * Creates a point initialized to zero.
     */
    public Point4f() {
    }

    /**
     * Creates a point with the specified values.
     *
     * @param x the x component
     * @param y the y component
     * @param z the z component
     * @param w the w component
     */
    public Point4f(float x, float y, float z, float w) {
        super(x, y, z, w);
    }
}
