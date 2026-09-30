package com.nttdocomo.ui.graphics3d.collision;

import com.nttdocomo.ui.graphics3d.Figure;
import com.nttdocomo.ui.util3d.Vector3D;

/**
 * Receives collision callbacks.
 */
public interface CollisionObserver {
    void onHit(Shape left, Shape right, boolean fromBackFace, Vector3D point);

    boolean onHit(Shape shape, int count, BoundingVolume[] volumes, int[] boneIds, boolean[] fromBackFace, Vector3D[] points);

    void onHit(Shape shape, Sphere sphere, float distance, Vector3D point, float travel);

    void onPick(Ray ray, Figure figure, IntersectionAttribute[] attributes);
}
