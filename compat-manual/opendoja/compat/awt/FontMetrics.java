package opendoja.compat.awt;

public class FontMetrics implements java.io.Serializable {
    private transient opendoja.compat.awt.Font font;
    private transient android.graphics.Paint paint;

    public FontMetrics() {
    }

    public FontMetrics(opendoja.compat.awt.Font font) {
        this.font = font;
    }

    public opendoja.compat.awt.Font getFont() {
        return font;
    }

    private android.graphics.Paint paint() {
        if (paint == null) {
            paint = new android.graphics.Paint();
            paint.setAntiAlias(true);
            paint.setSubpixelText(true);
            opendoja.compat.awt.Font f = font == null ? new opendoja.compat.awt.Font() : font;
            String family = f.getFamily();
            android.graphics.Typeface typeface;
            if (opendoja.compat.awt.Font.MONOSPACED.equals(family)) {
                typeface = android.graphics.Typeface.MONOSPACE;
            } else if (opendoja.compat.awt.Font.SERIF.equals(family)) {
                typeface = android.graphics.Typeface.SERIF;
            } else {
                typeface = android.graphics.Typeface.DEFAULT;
            }
            int style = android.graphics.Typeface.NORMAL;
            if (f.isBold()) {
                style |= android.graphics.Typeface.BOLD;
            }
            if (f.isItalic()) {
                style |= android.graphics.Typeface.ITALIC;
            }
            paint.setTypeface(android.graphics.Typeface.create(typeface, style));
            paint.setTextSize(f.getSize2D());
        }
        return paint;
    }

    public int getAscent() {
        return (int) Math.ceil(-paint().ascent());
    }

    public int getDescent() {
        return (int) Math.ceil(paint().descent());
    }

    public int getLeading() {
        return 0;
    }

    public int getHeight() {
        return getAscent() + getDescent() + getLeading();
    }

    public int stringWidth(java.lang.String p0) {
        if (p0 == null || p0.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(paint().measureText(p0));
    }

    public int charWidth(char c) {
        return stringWidth(String.valueOf(c));
    }
}
