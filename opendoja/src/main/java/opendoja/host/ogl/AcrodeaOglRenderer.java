package opendoja.host.ogl;

/**
 * OpenGL renderer variant that carries the Acrodea matrix-mode extension used
 * by some legacy mobile 3D libraries.
 */
public final class AcrodeaOglRenderer extends OglRenderer {
    private final AcrodeaOglMatrixExtension acrodeaMatrixExtension = new AcrodeaOglMatrixExtension();

    public AcrodeaOglRenderer(Host host) {
        super(host);
    }

    @Override
    public boolean acceptsExtensionMatrixMode(int mode) {
        return acrodeaMatrixExtension.acceptsMatrixMode(mode);
    }

    @Override
    public void onBeginDrawing() {
        acrodeaMatrixExtension.beginDrawing();
    }

    @Override
    public boolean usesExtensionMatrices(boolean standardModelViewConfigured, boolean standardProjectionConfigured) {
        return acrodeaMatrixExtension.usesMatrices(standardModelViewConfigured, standardProjectionConfigured);
    }

    @Override
    public float[] extensionWorldMatrix() {
        return acrodeaMatrixExtension.worldMatrix();
    }

    @Override
    public float[] extensionCameraMatrix() {
        return acrodeaMatrixExtension.cameraMatrix();
    }

    @Override
    public void loadExtensionMatrix(int mode, float[] matrix) {
        acrodeaMatrixExtension.loadMatrix(mode, matrix);
    }

    @Override
    public void multiplyExtensionMatrix(int mode, float[] matrix) {
        acrodeaMatrixExtension.multiplyMatrix(mode, matrix);
    }

    @Override
    public void pushExtensionMatrix(int mode) {
        acrodeaMatrixExtension.pushMatrix(mode);
    }

    @Override
    public boolean popExtensionMatrix(int mode) {
        return acrodeaMatrixExtension.popMatrix(mode);
    }
}
