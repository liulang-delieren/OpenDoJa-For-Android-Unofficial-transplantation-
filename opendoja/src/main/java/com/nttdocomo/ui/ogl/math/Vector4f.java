package com.nttdocomo.ui.ogl.math;

/**
 * Defines a 4D vector with float components.
 */
public final class Vector4f extends Tuple4f {
    /**
     * Creates a vector initialized to zero.
     */
    public Vector4f() {
    }

    /**
     * Creates a vector with the specified values.
     *
     * @param x the x component
     * @param y the y component
     * @param z the z component
     * @param w the w component
     */
    public Vector4f(float x, float y, float z, float w) {
        super(x, y, z, w);
    }
}
