package com.nttdocomo.ui.sound3d;

import com.nttdocomo.ui.util3d.Transform;
import com.nttdocomo.ui.util3d.Vector3D;

/**
 * Defines the listener position in Cartesian coordinates.
 */
public class CartesianListener {
    private final Vector3D eye = new Vector3D();
    private final Vector3D center = new Vector3D(0f, 0f, 1f);
    private final Vector3D up = new Vector3D(0f, 1f, 0f);
    private final Transform transform = new Transform();
    private final Vector3D velocity = new Vector3D();

    /**
     * Creates a listener object.
     */
    public CartesianListener() {
    }

    /**
     * Sets the listener orientation from eye, center, and up vectors.
     *
     * @param eye the eye position
     * @param center the look-at position
     * @param up the up vector
     */
    public void setLookAt(Vector3D eye, Vector3D center, Vector3D up) {
        if (eye == null || center == null || up == null) {
            throw new NullPointerException("vector");
        }
        this.eye.set(eye);
        this.center.set(center);
        this.up.set(up);
        transform.lookAt(eye, center, up);
    }

    /**
     * Sets the listener transform.
     *
     * @param transform the transform to set
     */
    public void setTransform(Transform transform) {
        if (transform == null) {
            throw new NullPointerException("transform");
        }
        this.transform.set(transform);
    }

    /**
     * Returns the listener velocity.
     *
     * @return the current velocity
     */
    public Vector3D getVelocity() {
        return new Vector3D(velocity);
    }

    /**
     * Sets the listener velocity.
     *
     * @param velocity the velocity to set
     */
    public void setVelocity(Vector3D velocity) {
        if (velocity == null) {
            throw new NullPointerException("velocity");
        }
        this.velocity.set(velocity);
    }
}
