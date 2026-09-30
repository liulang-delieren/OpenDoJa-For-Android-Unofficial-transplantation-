package opendoja.host.ogl;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Compatibility state for the Acrodea OpenGL ES matrix modes used by legacy
 * mobile 3D libraries.
 */
public final class AcrodeaOglMatrixExtension {
    private float[] worldMatrix = identityMatrix();
    private float[] cameraMatrix;
    private final Deque<float[]> worldStack = new ArrayDeque<>();
    private final Deque<float[]> cameraStack = new ArrayDeque<>();

    public boolean acceptsMatrixMode(int mode) {
        return OglExtensionMatrixMode.fromGlMatrixMode(mode) != null;
    }

    public void beginDrawing() {
        worldMatrix = identityMatrix();
        cameraMatrix = null;
    }

    public boolean usesMatrices(boolean standardModelViewConfigured, boolean standardProjectionConfigured) {
        return cameraMatrix != null && !standardModelViewConfigured && !standardProjectionConfigured;
    }

    public float[] worldMatrix() {
        return worldMatrix;
    }

    public float[] cameraMatrix() {
        return cameraMatrix;
    }

    public void loadMatrix(int mode, float[] matrix) {
        switch (requireMode(mode)) {
            case ACRODEA_WORLD -> worldMatrix = matrix;
            case ACRODEA_CAMERA -> cameraMatrix = matrix;
            default -> throw new IllegalArgumentException("Unsupported extension matrix mode: " + mode);
        }
    }

    public void multiplyMatrix(int mode, float[] matrix) {
        switch (requireMode(mode)) {
            case ACRODEA_WORLD -> worldMatrix = multiplyMatrices(worldMatrix, matrix);
            case ACRODEA_CAMERA -> cameraMatrix = multiplyMatrices(
                    cameraMatrix == null ? identityMatrix() : cameraMatrix,
                    matrix);
            default -> throw new IllegalArgumentException("Unsupported extension matrix mode: " + mode);
        }
    }

    public void pushMatrix(int mode) {
        switch (requireMode(mode)) {
            case ACRODEA_WORLD -> worldStack.push(worldMatrix.clone());
            case ACRODEA_CAMERA -> cameraStack.push(
                    (cameraMatrix == null ? identityMatrix() : cameraMatrix).clone());
            default -> throw new IllegalArgumentException("Unsupported extension matrix mode: " + mode);
        }
    }

    public boolean popMatrix(int mode) {
        switch (requireMode(mode)) {
            case ACRODEA_WORLD -> {
                if (worldStack.isEmpty()) {
                    return false;
                }
                worldMatrix = worldStack.pop();
                return true;
            }
            case ACRODEA_CAMERA -> {
                if (cameraStack.isEmpty()) {
                    return false;
                }
                cameraMatrix = cameraStack.pop();
                return true;
            }
            default -> throw new IllegalArgumentException("Unsupported extension matrix mode: " + mode);
        }
    }

    private static OglExtensionMatrixMode requireMode(int glMatrixMode) {
        OglExtensionMatrixMode mode = OglExtensionMatrixMode.fromGlMatrixMode(glMatrixMode);
        if (mode == null) {
            throw new IllegalArgumentException("Unsupported extension matrix mode: " + glMatrixMode);
        }
        return mode;
    }

    private static float[] identityMatrix() {
        float[] matrix = new float[16];
        matrix[0] = 1f;
        matrix[5] = 1f;
        matrix[10] = 1f;
        matrix[15] = 1f;
        return matrix;
    }

    private static float[] multiplyMatrices(float[] left, float[] right) {
        float[] product = new float[16];
        for (int column = 0; column < 4; column++) {
            int columnOffset = column * 4;
            for (int row = 0; row < 4; row++) {
                product[columnOffset + row] =
                        left[row] * right[columnOffset]
                                + left[row + 4] * right[columnOffset + 1]
                                + left[row + 8] * right[columnOffset + 2]
                                + left[row + 12] * right[columnOffset + 3];
            }
        }
        return product;
    }
}
