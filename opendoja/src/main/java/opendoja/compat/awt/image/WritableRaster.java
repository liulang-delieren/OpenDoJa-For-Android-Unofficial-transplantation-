package opendoja.compat.awt.image;

public class WritableRaster extends opendoja.compat.awt.image.Raster {
    public WritableRaster() {
    }

    public WritableRaster(int width, int height, int[] samples) {
        super(width, height, samples);
    }

    public opendoja.compat.awt.image.DataBuffer getDataBuffer() {
        return null;
    }
}
