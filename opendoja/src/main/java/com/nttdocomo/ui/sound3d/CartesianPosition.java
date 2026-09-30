package com.nttdocomo.ui.sound3d;

import com.nttdocomo.ui.Audio3DLocalization;
import com.nttdocomo.ui.util3d.Vector3D;

/**
 * Represents a sound source in Cartesian coordinates.
 */
public class CartesianPosition implements SoundPosition, Audio3DLocalization {
    private static float defaultCoordinateFactor = 1f;

    private final CartesianListener listener;
    private final Vector3D position = new Vector3D();
    private final Vector3D velocity = new Vector3D();
    private final float coordinateFactor;

    /**
     * Creates a position object using the default coordinate factor.
     *
     * @param listener the listener to associate with this position
     */
    public CartesianPosition(CartesianListener listener) {
        this(listener, defaultCoordinateFactor);
    }

    /**
     * Creates a position object.
     *
     * @param listener the listener to associate with this position
     * @param coordinateFactor the coordinate factor
     */
    public CartesianPosition(CartesianListener listener, float coordinateFactor) {
        this.listener = listener;
        this.coordinateFactor = coordinateFactor;
    }

    /**
     * Returns the associated listener.
     *
     * @return the listener, or {@code null} if none was specified
     */
    public CartesianListener getListener() {
        return listener;
    }

    /**
     * Sets the default coordinate factor.
     *
     * @param factor the factor to use for newly created instances
     */
    public static void setDefaultCoordinateFactor(float factor) {
        defaultCoordinateFactor = factor;
    }

    /**
     * Returns the coordinate factor.
     *
     * @return the coordinate factor
     */
    public float getCoordinateFactor() {
        return coordinateFactor;
    }

    /**
     * Sets the sound source position.
     *
     * @param position the position to set
     */
    public void setPosition(Vector3D position) {
        if (position == null) {
            throw new NullPointerException("position");
        }
        this.position.set(position);
    }

    /**
     * Returns the sound source position.
     *
     * @return the current position
     */
    public Vector3D getPosition() {
        return new Vector3D(position);
    }

    /**
     * Returns the current velocity.
     *
     * @return the velocity
     */
    public Vector3D getVelocity() {
        return new Vector3D(velocity);
    }

    /**
     * Sets the velocity.
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
