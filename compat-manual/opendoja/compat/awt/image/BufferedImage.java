package opendoja.compat.awt.image;

public class BufferedImage extends opendoja.compat.awt.Image implements opendoja.compat.awt.image.WritableRenderedImage, opendoja.compat.awt.Transparency {
    public static final int TYPE_CUSTOM = 0;
    public static final int TYPE_INT_RGB = 1;
    public static final int TYPE_INT_ARGB = 2;
    public static final int TYPE_INT_ARGB_PRE = 3;
    public static final int TYPE_INT_BGR = 4;
    public static final int TYPE_3BYTE_BGR = 5;
    public static final int TYPE_4BYTE_ABGR = 6;
    public static final int TYPE_4BYTE_ABGR_PRE = 7;
    public static final int TYPE_USHORT_565_RGB = 8;
    public static final int TYPE_USHORT_555_RGB = 9;
    public static final int TYPE_BYTE_GRAY = 10;
    public static final int TYPE_USHORT_GRAY = 11;
    public static final int TYPE_BYTE_BINARY = 12;
    public static final int TYPE_BYTE_INDEXED = 13;

    private transient android.graphics.Bitmap bitmap;
    private final int type;
    private int width;
    private int height;
    private transient opendoja.compat.awt.image.ColorModel colorModel;
    private transient opendoja.compat.awt.image.WritableRaster raster;

    public BufferedImage() {
        this(1, 1, TYPE_INT_ARGB);
    }

    public BufferedImage(int width, int height, int type) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width/height must be positive: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.type = type;
        this.bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
    }

    public BufferedImage(opendoja.compat.awt.image.ColorModel cm, opendoja.compat.awt.image.WritableRaster raster,
                         boolean isRasterPremultiplied, java.util.Hashtable<?, ?> properties) {
        // The stub API exposes no raster dimensions, so raster-backed construction is only
        // reachable from OGL/paletted paths handled later; keep a valid 1x1 surface for now.
        this(1, 1, TYPE_INT_ARGB);
        if (cm != null) {
            this.colorModel = cm;
        }
        this.raster = raster;
    }

    /** Wraps a decoded bitmap (ImageIO/BitmapFactory result). */
    public BufferedImage(android.graphics.Bitmap source) {
        this.type = TYPE_INT_ARGB;
        initWithBitmap(source);
    }

    /** Wraps a decoded bitmap together with its palette color model and index raster. */
    public BufferedImage(android.graphics.Bitmap source, opendoja.compat.awt.image.ColorModel colorModel,
                         opendoja.compat.awt.image.WritableRaster raster) {
        this.type = TYPE_INT_ARGB;
        initWithBitmap(source);
        if (colorModel != null) {
            this.colorModel = colorModel;
        }
        this.raster = raster;
    }

    private void initWithBitmap(android.graphics.Bitmap source) {
        if (source == null) {
            throw new IllegalArgumentException("bitmap source must not be null");
        }
        android.graphics.Bitmap usable = source;
        if (source.getConfig() != android.graphics.Bitmap.Config.ARGB_8888) {
            android.graphics.Bitmap copy = source.copy(android.graphics.Bitmap.Config.ARGB_8888, false);
            if (copy != null) {
                usable = copy;
            }
        }
        this.bitmap = usable;
        this.width = usable.getWidth();
        this.height = usable.getHeight();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getType() {
        return type;
    }

    public android.graphics.Bitmap getBitmap() {
        return bitmap;
    }

    public int getRGB(int x, int y) {
        return bitmap.getPixel(x, y);
    }

    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        if (w <= 0 || h <= 0) {
            return rgbArray == null ? new int[0] : rgbArray;
        }
        int[] pixels = rgbArray;
        if (pixels == null) {
            pixels = new int[offset + (h * scansize)];
        }
        int[] row = new int[w];
        for (int rowIdx = 0; rowIdx < h; rowIdx++) {
            bitmap.getPixels(row, 0, w, startX, startY + rowIdx, w, 1);
            System.arraycopy(row, 0, pixels, offset + (rowIdx * scansize), w);
        }
        return pixels;
    }

    public void setRGB(int x, int y, int rgb) {
        bitmap.setPixel(x, y, rgb);
    }

    public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        if (w <= 0 || h <= 0 || rgbArray == null) {
            return;
        }
        int[] row = new int[w];
        for (int rowIdx = 0; rowIdx < h; rowIdx++) {
            System.arraycopy(rgbArray, offset + (rowIdx * scansize), row, 0, w);
            bitmap.setPixels(row, 0, w, startX, startY + rowIdx, w, 1);
        }
    }

    public opendoja.compat.awt.Graphics2D createGraphics() {
        return new opendoja.compat.awt.Graphics2D(this);
    }

    public opendoja.compat.awt.image.ColorModel getColorModel() {
        if (colorModel == null) {
            colorModel = new opendoja.compat.awt.image.ColorModel();
        }
        return colorModel;
    }

    public opendoja.compat.awt.image.WritableRaster getRaster() {
        return raster;
    }

    public opendoja.compat.awt.image.BufferedImage getSubimage(int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("w/h must be positive");
        }
        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, x, y, w, h);
        BufferedImage sub = new BufferedImage(w, h, type);
        sub.bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return sub;
    }

    public int getTransparency() {
        return TRANSLUCENT;
    }

    public void flush() {
    }
}
