package com.nttdocomo.ui.graphics3d;

import opendoja.g3d.SoftwareTexture;

/**
 * Internal bridge that exposes package-private {@link Primitive} render state
 * to the parent graphics package without widening {@link Primitive}'s public
 * API.
 */
public final class _PrimitiveRenderStateAccess {
    private _PrimitiveRenderStateAccess() {
    }

    public static PrimitiveRenderState snapshot(Primitive primitive) {
        return new PrimitiveRenderState(
                primitive.textureHandle(),
                primitive.textureWrapEnabled(),
                primitive.textureCoordinateTranslateU(),
                primitive.textureCoordinateTranslateV(),
                primitive.depthTestEnabled(),
                primitive.depthWriteEnabled(),
                primitive.doubleSided()
        );
    }

    public record PrimitiveRenderState(SoftwareTexture textureHandle, boolean textureWrapEnabled,
                                       float textureCoordinateTranslateU, float textureCoordinateTranslateV,
                                       boolean depthTestEnabled, boolean depthWriteEnabled,
                                       boolean doubleSided) {
    }
}
