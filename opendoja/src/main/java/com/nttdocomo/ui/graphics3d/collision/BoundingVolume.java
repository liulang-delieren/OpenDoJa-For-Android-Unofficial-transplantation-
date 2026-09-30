package com.nttdocomo.ui.graphics3d.collision;

import com.nttdocomo.ui.util3d.Vector3D;

/**
 * Defines the common interface of bounding volumes.
 */
public interface BoundingVolume extends Shape {
    int ROTATE_NONE = 0;
    int ROTATE_X = 1;
    int ROTATE_Y = 2;
    int ROTATE_Z = 3;
    int ROTATE_XY = 4;
    int ROTATE_YX = 5;

    void setCenter(Vector3D center);

    Vector3D getCenter(boolean transformed);

    void setRotate(int rotate);

    int getRotate();

    float getEffectiveRadius(Vector3D direction);

    void setHittingFromBackFaceEnabled(boolean enabled);

    boolean isHittingFromBackFaceEnabled();
}
