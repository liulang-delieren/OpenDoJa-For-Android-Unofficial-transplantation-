package com.nttdocomo.ui.util3d;

/**
 * Internal bridge for non-public transform state needed by the UI package.
 */
public final class _TransformInternalAccess {
    private _TransformInternalAccess() {
    }

    public static float[] raw(Transform transform) {
        return transform.raw();
    }
}
