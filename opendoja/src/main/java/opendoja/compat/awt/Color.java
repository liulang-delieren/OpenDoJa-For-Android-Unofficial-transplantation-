package opendoja.compat.awt;

public class Color implements opendoja.compat.awt.Paint, java.io.Serializable {
    public static opendoja.compat.awt.Color BLACK = new Color(0, 0, 0, 255);
    public static opendoja.compat.awt.Color WHITE = new Color(255, 255, 255, 255);
    public static opendoja.compat.awt.Color RED = new Color(255, 0, 0, 255);
    public static opendoja.compat.awt.Color GREEN = new Color(0, 255, 0, 255);
    public static opendoja.compat.awt.Color BLUE = new Color(0, 0, 255, 255);
    public static opendoja.compat.awt.Color CYAN = new Color(0, 255, 255, 255);
    public static opendoja.compat.awt.Color MAGENTA = new Color(255, 0, 255, 255);
    public static opendoja.compat.awt.Color YELLOW = new Color(255, 255, 0, 255);
    public static opendoja.compat.awt.Color ORANGE = new Color(255, 200, 0, 255);
    public static opendoja.compat.awt.Color PINK = new Color(255, 175, 175, 255);
    public static opendoja.compat.awt.Color LIGHT_GRAY = new Color(192, 192, 192, 255);
    public static opendoja.compat.awt.Color GRAY = new Color(128, 128, 128, 255);
    public static opendoja.compat.awt.Color DARK_GRAY = new Color(64, 64, 64, 255);

    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public Color() {
        this(0, 0, 0, 255);
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    public Color(int rgb) {
        this((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255);
    }

    public Color(int rgb, boolean hasalpha) {
        this((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, hasalpha ? ((rgb >>> 24) & 0xFF) : 255);
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    public int getRGB() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int getRed() {
        return r;
    }

    public int getGreen() {
        return g;
    }

    public int getBlue() {
        return b;
    }

    public int getAlpha() {
        return a;
    }

    public int getTransparency() {
        if (a == 255) {
            return OPAQUE;
        }
        return a == 0 ? BITMASK : TRANSLUCENT;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Color other)) {
            return false;
        }
        return r == other.r && g == other.g && b == other.b && a == other.a;
    }

    @Override
    public int hashCode() {
        return getRGB();
    }

    @Override
    public String toString() {
        return "opendoja.compat.awt.Color[r=" + r + ",g=" + g + ",b=" + b + ",a=" + a + "]";
    }
}
