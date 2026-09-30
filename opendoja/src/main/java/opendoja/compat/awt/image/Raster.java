package opendoja.compat.awt.image;

/**
 * Pixel-sample container replacing the generated no-op shim. Indexed images (GIF palettes)
 * read their palette indices through {@link #getSample}.
 */
public class Raster {
    protected int width;
    protected int height;
    protected int[] samples;

    public Raster() {
    }

    public Raster(int width, int height, int[] samples) {
        this.width = width;
        this.height = height;
        this.samples = samples;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSample(int x, int y, int band) {
        if (samples == null || x < 0 || y < 0 || x >= width || y >= height) {
            return 0;
        }
        return samples[y * width + x];
    }

    public static WritableRaster createInterleavedRaster(opendoja.compat.awt.image.DataBuffer dataBuffer,
                                                         int width, int height, int scanlineStride,
                                                         int pixelStride, int[] bandOffsets,
                                                         opendoja.compat.awt.Point location) {
        // DataBuffer-backed raster construction belongs to the OGL/texture stage; software
        // callers handle the null return exactly as they did with the stub shim.
        return null;
    }
}
