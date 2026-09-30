package opendoja.host.ogl;

import opendoja.compat.awt.Rectangle;

final class OglHardwareBackend {
    OglHardwareBackend(OglRenderer owner) {
    }

    void prepareForSoftwareMutation() {
    }

    void flush() {
    }

    boolean draw(int mode, int first, int count, OglRenderer.OglIndexSource indexSource) {
        return false;
    }

    boolean clear(int mask) {
        return false;
    }

    void endDrawing() {
    }

    void onTextureDeleted(int textureId) {
    }

    void onPresentedSoftwareOverlay(Rectangle bounds) {
    }

    void onSoftwareSurfaceMutation() {
    }

    void onHostDelegateRecreated() {
    }

    void close() {
    }

    boolean hasBufferedHardwarePresentation() {
        return false;
    }
}
