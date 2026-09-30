package opendoja.compat.awt;

public class Font implements java.io.Serializable {
    public static final java.lang.String DIALOG = "Dialog";
    public static final java.lang.String DIALOG_INPUT = "DialogInput";
    public static final java.lang.String SANS_SERIF = "SansSerif";
    public static final java.lang.String SERIF = "Serif";
    public static final java.lang.String MONOSPACED = "Monospaced";
    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int ROMAN_BASELINE = 0;
    public static final int CENTER_BASELINE = 1;
    public static final int HANGING_BASELINE = 2;
    public static final int TRUETYPE_FONT = 0;
    public static final int TYPE1_FONT = 1;
    public static final int LAYOUT_LEFT_TO_RIGHT = 0;
    public static final int LAYOUT_RIGHT_TO_LEFT = 1;
    public static final int LAYOUT_NO_START_CONTEXT = 2;
    public static final int LAYOUT_NO_LIMIT_CONTEXT = 4;

    private final String name;
    private final int style;
    private final float size;

    public Font() {
        this(DIALOG, PLAIN, 12);
    }

    public Font(java.lang.String name, int style, int size) {
        this(name, style, (float) size);
    }

    public Font(java.lang.String name, int style, float size) {
        this.name = name == null ? DIALOG : name;
        this.style = style;
        this.size = size;
    }

    public opendoja.compat.awt.Font deriveFont(int newStyle, float newSize) {
        return new Font(name, newStyle, newSize);
    }

    public opendoja.compat.awt.Font deriveFont(float newSize) {
        return new Font(name, style, newSize);
    }

    public String getName() {
        return name;
    }

    public String getFamily() {
        return name;
    }

    public String getFontName() {
        return name;
    }

    public int getStyle() {
        return style;
    }

    public int getSize() {
        return (int) Math.ceil(size);
    }

    public float getSize2D() {
        return size;
    }

    public boolean isPlain() {
        return style == PLAIN;
    }

    public boolean isBold() {
        return (style & BOLD) != 0;
    }

    public boolean isItalic() {
        return (style & ITALIC) != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Font other)) {
            return false;
        }
        return style == other.style && Float.compare(size, other.size) == 0
                && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return ((name.hashCode() * 31) + style) * 31 + Float.floatToIntBits(size);
    }

    @Override
    public String toString() {
        return "opendoja.compat.awt.Font[name=" + name + ",style=" + style + ",size=" + size + "]";
    }
}
