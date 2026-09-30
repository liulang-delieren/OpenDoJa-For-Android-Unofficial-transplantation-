package com.nttdocomo.opt.ui.j3d;

import opendoja.g3d.MascotFigure;
import opendoja.g3d.SoftwareTexture;

/**
 * Internal bridge for non-public opt.ui.j3d state needed by the UI package.
 */
public final class _Opt3DInternalAccess {
    private _Opt3DInternalAccess() {
    }

    public static float[] toFloatMatrix(AffineTrans transform) {
        return transform.toFloatMatrix();
    }

    public static MascotFigure handle(Figure figure) {
        return figure.handle();
    }

    public static SoftwareTexture handle(Texture texture) {
        return texture.handle();
    }
}
