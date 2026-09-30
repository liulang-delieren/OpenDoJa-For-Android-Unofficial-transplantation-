package com.nttdocomo.ui.sound3d;

import com.nttdocomo.ui.Audio3DLocalization;
import com.nttdocomo.ui.util3d.Vector3D;

/**
 * Represents a sound source in polar coordinates.
 */
public class PolarPosition implements SoundPosition, Audio3DLocalization {
    private static float defaultCoordinateFactor = 1f;

    private final float coordinateFactor;
    private Vector3D velocity;
    private float distance;
    private float direction;
    private float elevation;

    /**
     * Creates a position object using the default coordinate factor.
     */
    public PolarPosition() {
        this(defaultCoordinateFactor);
    }

    /**
     * Creates a position object.
     *
     * @param coordinateFactor the coordinate factor
     */
    public PolarPosition(float coordinateFactor) {
        requirePositiveFinite(coordinateFactor, "coordinateFactor");
        this.coordinateFactor = coordinateFactor;
    }

    /**
     * Sets the default coordinate factor.
     *
     * @param coordinateFactor the factor to use for newly created instances
     */
    public static void setDefaultCoordinateFactor(float coordinateFactor) {
        requirePositiveFinite(coordinateFactor, "coordinateFactor");
        defaultCoordinateFactor = coordinateFactor;
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
     * Sets the distance component.
     *
     * @param distance the distance to set
     */
    public void setDistance(float distance) {
        if (Float.isNaN(distance) || distance < 0f) {
            throw new IllegalArgumentException("distance");
        }
        this.distance = distance;
    }

    /**
     * Returns the distance component.
     *
     * @return the distance
     */
    public float getDistance() {
        return distance;
    }

    /**
     * Sets the direction component.
     *
     * @param direction the direction to set
     */
    public void setDirection(float direction) {
        requireFinite(direction, "direction");
        this.direction = direction;
    }

    /**
     * Returns the direction component.
     *
     * @return the direction
     */
    public float getDirection() {
        return direction;
    }

    /**
     * Sets the elevation component.
     *
     * @param elevation the elevation to set
     */
    public void setElevation(float elevation) {
        requireFinite(elevation, "elevation");
        this.elevation = elevation;
    }

    /**
     * Returns the elevation component.
     *
     * @return the elevation
     */
    public float getElevation() {
        return elevation;
    }

    /**
     * Sets the polar position from a vector containing distance, direction, and
     * elevation components.
     *
     * @param vector the polar position components
     */
    public void setPosition(Vector3D vector) {
        if (vector == null) {
            throw new NullPointerException("vector");
        }
        setDistance(vector.getX());
        setDirection(vector.getY());
        setElevation(vector.getZ());
    }

    /**
     * Returns the current velocity.
     *
     * @return the velocity
     */
    public Vector3D getVelocity() {
        return velocity == null ? null : new Vector3D(velocity);
    }

    /**
     * Sets the velocity.
     *
     * @param velocity the velocity to set, or {@code null} to clear it
     */
    public void setVelocity(Vector3D velocity) {
        if (velocity == null) {
            this.velocity = null;
            return;
        }
        this.velocity = new Vector3D(velocity);
    }

    private static void requirePositiveFinite(float value, String name) {
        if (!Float.isFinite(value) || value <= 0f) {
            throw new IllegalArgumentException(name);
        }
    }

    private static void requireFinite(float value, String name) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException(name);
        }
    }
}
