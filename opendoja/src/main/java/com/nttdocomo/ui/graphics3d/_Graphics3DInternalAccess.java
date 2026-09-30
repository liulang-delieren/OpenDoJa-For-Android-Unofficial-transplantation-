package com.nttdocomo.ui.graphics3d;

import opendoja.g3d.MascotFigure;

/**
 * Internal bridge for non-public graphics3d state needed by the parent UI
 * package.
 */
public final class _Graphics3DInternalAccess {
    private _Graphics3DInternalAccess() {
    }

    public static MascotFigure handle(Figure figure) {
        return figure.handle();
    }

    public static DrawableRenderState drawableRenderState(DrawableObject3D object) {
        return new DrawableRenderState(object.blendModeValue(), object.transparencyValue());
    }

    public static LightState lightState(Light light) {
        return new LightState(light.mode(), light.intensity(), light.color());
    }

    public static FogState fogState(Fog fog) {
        return new FogState(fog.mode(), fog.linearNear(), fog.linearFar(), fog.density(), fog.color());
    }

    public record DrawableRenderState(int blendMode, float transparency) {
    }

    public record LightState(int mode, float intensity, int color) {
    }

    public record FogState(int mode, float linearNear, float linearFar, float density, int color) {
    }
}
