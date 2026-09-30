package opendoja.compat.imageio;

public class ImageWriteParam {
    public static final int MODE_COPY_FROM_METADATA = -1;
    public static final int MODE_DEFAULT = 0;
    public static final int MODE_EXPLICIT = 1;

    private int compressionMode = MODE_DEFAULT;
    private float compressionQuality = 0.75f;

    public boolean canWriteCompressed() {
        return true;
    }

    public void setCompressionMode(int mode) {
        this.compressionMode = mode;
    }

    public int getCompressionMode() {
        return compressionMode;
    }

    public void setCompressionQuality(float quality) {
        this.compressionQuality = Math.max(0f, Math.min(1f, quality));
    }

    public float getCompressionQuality() {
        return compressionQuality;
    }
}
