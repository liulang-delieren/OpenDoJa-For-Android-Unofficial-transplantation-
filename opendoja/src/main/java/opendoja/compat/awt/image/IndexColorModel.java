package opendoja.compat.awt.image;

/**
 * Real palette-backed color model (java.awt semantics). Replaces the generated no-op shim:
 * PalettedImage/SoftwareTexture rely on getMapSize/getRGB/getTransparentPixel.
 */
public class IndexColorModel extends opendoja.compat.awt.image.ColorModel {
    private final int mapSize;
    private final int[] palette;
    private final int transparentPixel;

    public IndexColorModel() {
        this.mapSize = 0;
        this.palette = new int[0];
        this.transparentPixel = -1;
    }

    /** bits, size, red, green, blue component arrays plus transparent pixel index (-1 for none). */
    public IndexColorModel(int bits, int size, byte[] red, byte[] green, byte[] blue, int transparent) {
        int count = Math.max(0, size);
        this.mapSize = count;
        this.palette = new int[count];
        for (int i = 0; i < count; i++) {
            int r = component(red, i);
            int g = component(green, i);
            int b = component(blue, i);
            this.palette[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        this.transparentPixel = transparent;
    }

    /** bits, size, red, green, blue, alpha component arrays. */
    public IndexColorModel(int bits, int size, byte[] red, byte[] green, byte[] blue, byte[] alpha) {
        int count = Math.max(0, size);
        this.mapSize = count;
        this.palette = new int[count];
        for (int i = 0; i < count; i++) {
            int a = component(alpha, i);
            int r = component(red, i);
            int g = component(green, i);
            int b = component(blue, i);
            this.palette[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        this.transparentPixel = -1;
    }

    private static int component(byte[] values, int index) {
        if (values == null || index < 0 || index >= values.length) {
            return 0;
        }
        return values[index] & 0xFF;
    }

    public int getMapSize() {
        return mapSize;
    }

    public int getRGB(int pixel) {
        if (pixel < 0 || pixel >= mapSize) {
            return 0;
        }
        if (pixel == transparentPixel) {
            return 0x00000000;
        }
        return palette[pixel];
    }

    public int getTransparentPixel() {
        return transparentPixel;
    }
}
