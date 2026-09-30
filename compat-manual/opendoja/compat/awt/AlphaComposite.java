package opendoja.compat.awt;

public class AlphaComposite implements opendoja.compat.awt.Composite {
    public static final int CLEAR = 1;
    public static final int SRC = 2;
    public static final int SRC_OVER = 3;
    public static final int DST_OVER = 4;
    public static final int SRC_IN = 5;
    public static final int DST_IN = 6;
    public static final int SRC_OUT = 7;
    public static final int DST_OUT = 8;
    public static final int DST = 9;
    public static final int SRC_ATOP = 10;
    public static final int DST_ATOP = 11;
    public static final int XOR = 12;

    public static opendoja.compat.awt.AlphaComposite SrcOver = new AlphaComposite(SRC_OVER, 1.0f);
    public static opendoja.compat.awt.AlphaComposite Clear = new AlphaComposite(CLEAR, 0.0f);

    private final int rule;
    private final float alpha;

    public AlphaComposite() {
        this(SRC_OVER, 1.0f);
    }

    private AlphaComposite(int rule, float alpha) {
        this.rule = rule;
        this.alpha = alpha < 0f ? 0f : (alpha > 1f ? 1f : alpha);
    }

    public static opendoja.compat.awt.AlphaComposite getInstance(int rule, float alpha) {
        if (rule == SRC_OVER && alpha >= 1f) {
            return SrcOver;
        }
        return new AlphaComposite(rule, alpha);
    }

    public int getRule() {
        return rule;
    }

    public float getAlpha() {
        return alpha;
    }

    public opendoja.compat.awt.AlphaComposite derive(int newRule) {
        return getInstance(newRule, alpha);
    }

    public opendoja.compat.awt.AlphaComposite derive(float newAlpha) {
        return getInstance(rule, newAlpha);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AlphaComposite other)) {
            return false;
        }
        return rule == other.rule && Float.compare(alpha, other.alpha) == 0;
    }

    @Override
    public int hashCode() {
        return (rule * 31) + Float.floatToIntBits(alpha);
    }

    @Override
    public String toString() {
        return "opendoja.compat.awt.AlphaComposite[rule=" + rule + ",alpha=" + alpha + "]";
    }
}
