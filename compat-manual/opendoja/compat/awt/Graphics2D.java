package opendoja.compat.awt;

public class Graphics2D extends opendoja.compat.awt.Graphics {
    private final opendoja.compat.awt.image.BufferedImage target;
    private final android.graphics.Canvas canvas;
    private final android.graphics.Paint paint;
    private final android.graphics.Matrix matrix;
    private opendoja.compat.awt.RenderingHints hints;
    private opendoja.compat.awt.Color color;
    private opendoja.compat.awt.Font font;
    private opendoja.compat.awt.AlphaComposite composite;
    private android.graphics.RectF clipDevice;
    private boolean clipDirty;
    private boolean stateDirty;
    private int initSave;
    private boolean disposed;
    private boolean antiAlias;
    private boolean textAntiAlias = true;
    private boolean filterBitmap;

    public Graphics2D() {
        this.target = null;
        this.canvas = null;
        this.paint = new android.graphics.Paint();
        this.matrix = new android.graphics.Matrix();
        this.hints = new opendoja.compat.awt.RenderingHints();
        this.color = opendoja.compat.awt.Color.BLACK;
        this.font = new opendoja.compat.awt.Font();
        this.composite = opendoja.compat.awt.AlphaComposite.SrcOver;
        paint.setTextSize(12f);
    }

    public Graphics2D(opendoja.compat.awt.image.BufferedImage target) {
        if (target == null || target.getBitmap() == null) {
            throw new IllegalArgumentException("target");
        }
        this.target = target;
        this.canvas = new android.graphics.Canvas(target.getBitmap());
        this.paint = new android.graphics.Paint();
        this.matrix = new android.graphics.Matrix();
        this.hints = new opendoja.compat.awt.RenderingHints();
        this.color = opendoja.compat.awt.Color.BLACK;
        this.font = new opendoja.compat.awt.Font();
        this.composite = opendoja.compat.awt.AlphaComposite.SrcOver;
        paint.setStyle(android.graphics.Paint.Style.FILL);
        paint.setStrokeWidth(1f);
        paint.setTextSize(font.getSize2D());
        this.initSave = canvas.save();
        applyColorAndComposite();
    }

    private boolean ready() {
        return !disposed && canvas != null && target != null;
    }

    private void syncState() {
        if (!stateDirty) {
            return;
        }
        canvas.restoreToCount(initSave);
        if (clipDevice != null) {
            canvas.clipRect(clipDevice);
        }
        canvas.setMatrix(matrix);
        stateDirty = false;
    }

    private void applyColorAndComposite() {
        paint.setColor(color.getRGB());
        float ca = composite.getAlpha();
        if (ca < 1f) {
            paint.setAlpha(Math.round((color.getRGB() >>> 24) * ca));
        }
        int rule = composite.getRule();
        if (rule == opendoja.compat.awt.AlphaComposite.SRC_OVER) {
            paint.setXfermode(null);
        } else {
            paint.setXfermode(new android.graphics.PorterDuffXfermode(ruleToMode(rule)));
        }
    }

    private static android.graphics.PorterDuff.Mode ruleToMode(int rule) {
        switch (rule) {
            case opendoja.compat.awt.AlphaComposite.CLEAR:
                return android.graphics.PorterDuff.Mode.CLEAR;
            case opendoja.compat.awt.AlphaComposite.SRC:
                return android.graphics.PorterDuff.Mode.SRC;
            case opendoja.compat.awt.AlphaComposite.DST_OVER:
                return android.graphics.PorterDuff.Mode.DST_OVER;
            case opendoja.compat.awt.AlphaComposite.SRC_IN:
                return android.graphics.PorterDuff.Mode.SRC_IN;
            case opendoja.compat.awt.AlphaComposite.DST_IN:
                return android.graphics.PorterDuff.Mode.DST_IN;
            case opendoja.compat.awt.AlphaComposite.SRC_OUT:
                return android.graphics.PorterDuff.Mode.SRC_OUT;
            case opendoja.compat.awt.AlphaComposite.DST_OUT:
                return android.graphics.PorterDuff.Mode.DST_OUT;
            case opendoja.compat.awt.AlphaComposite.DST:
                return android.graphics.PorterDuff.Mode.DST;
            case opendoja.compat.awt.AlphaComposite.SRC_ATOP:
                return android.graphics.PorterDuff.Mode.SRC_ATOP;
            case opendoja.compat.awt.AlphaComposite.DST_ATOP:
                return android.graphics.PorterDuff.Mode.DST_ATOP;
            case opendoja.compat.awt.AlphaComposite.XOR:
                return android.graphics.PorterDuff.Mode.XOR;
            default:
                return android.graphics.PorterDuff.Mode.SRC_OVER;
        }
    }

    private android.graphics.Paint prepare(android.graphics.Paint.Style style) {
        if (!ready()) {
            return null;
        }
        syncState();
        paint.setStyle(style);
        return paint;
    }

    public void setColor(opendoja.compat.awt.Color p0) {
        this.color = p0 == null ? opendoja.compat.awt.Color.BLACK : p0;
        applyColorAndComposite();
    }

    public opendoja.compat.awt.Color getColor() {
        return color;
    }

    public void setFont(opendoja.compat.awt.Font p0) {
        this.font = p0 == null ? new opendoja.compat.awt.Font() : p0;
        paint.setTextSize(this.font.getSize2D());
    }

    public opendoja.compat.awt.FontMetrics getFontMetrics() {
        return new opendoja.compat.awt.FontMetrics(font);
    }

    public void setComposite(opendoja.compat.awt.Composite p0) {
        if (p0 instanceof opendoja.compat.awt.AlphaComposite ac) {
            this.composite = ac;
        } else {
            this.composite = opendoja.compat.awt.AlphaComposite.SrcOver;
        }
        applyColorAndComposite();
    }

    public opendoja.compat.awt.Composite getComposite() {
        return composite;
    }

    public void setRenderingHint(opendoja.compat.awt.RenderingHints.Key p0, java.lang.Object p1) {
        if (p0 == null) {
            return;
        }
        hints.put(p0, p1);
        if (p0 == opendoja.compat.awt.RenderingHints.KEY_ANTIALIASING) {
            antiAlias = p1 == opendoja.compat.awt.RenderingHints.VALUE_ANTIALIAS_ON;
            paint.setAntiAlias(antiAlias);
        } else if (p0 == opendoja.compat.awt.RenderingHints.KEY_TEXT_ANTIALIASING) {
            textAntiAlias = p1 != opendoja.compat.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        } else if (p0 == opendoja.compat.awt.RenderingHints.KEY_INTERPOLATION) {
            filterBitmap = p1 != opendoja.compat.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        }
        paint.setFilterBitmap(filterBitmap);
    }

    public java.lang.Object getRenderingHint(opendoja.compat.awt.RenderingHints.Key p0) {
        return hints.get(p0);
    }

    public void setClip(int x, int y, int w, int h) {
        if (!ready()) {
            return;
        }
        if (w <= 0 || h <= 0) {
            clipDevice = new android.graphics.RectF();
            clipDevice.setEmpty();
            stateDirty = true;
            return;
        }
        clipDevice = deviceRect(x, y, w, h);
        stateDirty = true;
    }

    public void clipRect(int x, int y, int w, int h) {
        if (!ready() || w <= 0 || h <= 0) {
            return;
        }
        android.graphics.RectF next = deviceRect(x, y, w, h);
        if (clipDevice == null) {
            clipDevice = next;
        } else {
            float l = Math.max(clipDevice.left, next.left);
            float t = Math.max(clipDevice.top, next.top);
            float r = Math.min(clipDevice.right, next.right);
            float b = Math.min(clipDevice.bottom, next.bottom);
            if (l >= r || t >= b) {
                clipDevice.setEmpty();
            } else {
                clipDevice.set(l, t, r, b);
            }
        }
        stateDirty = true;
    }

    public void setClip(opendoja.compat.awt.Shape p0) {
        if (!ready()) {
            return;
        }
        if (p0 == null) {
            clipDevice = null;
            stateDirty = true;
            return;
        }
        opendoja.compat.awt.Rectangle bounds = p0.getBounds();
        if (bounds == null || bounds.isEmpty()) {
            setClip(0, 0, 0, 0);
            return;
        }
        setClip(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public opendoja.compat.awt.Shape getClip() {
        return getClipBounds();
    }

    public opendoja.compat.awt.Rectangle getClipBounds() {
        if (clipDevice == null) {
            return null;
        }
        android.graphics.Matrix inverse = new android.graphics.Matrix();
        if (!matrix.invert(inverse)) {
            return null;
        }
        float[] pts = {
                clipDevice.left, clipDevice.top,
                clipDevice.right, clipDevice.top,
                clipDevice.right, clipDevice.bottom,
                clipDevice.left, clipDevice.bottom
        };
        inverse.mapPoints(pts);
        float minX = pts[0];
        float minY = pts[1];
        float maxX = pts[0];
        float maxY = pts[1];
        for (int i = 1; i < 4; i++) {
            minX = Math.min(minX, pts[i * 2]);
            minY = Math.min(minY, pts[i * 2 + 1]);
            maxX = Math.max(maxX, pts[i * 2]);
            maxY = Math.max(maxY, pts[i * 2 + 1]);
        }
        int ix = (int) Math.floor(minX);
        int iy = (int) Math.floor(minY);
        return new opendoja.compat.awt.Rectangle(ix, iy,
                (int) Math.ceil(maxX) - ix, (int) Math.ceil(maxY) - iy);
    }

    private android.graphics.RectF deviceRect(int x, int y, int w, int h) {
        float[] pts = {x, y, x + w, y, x + w, y + h, x, y + h};
        matrix.mapPoints(pts);
        float minX = pts[0];
        float minY = pts[1];
        float maxX = pts[0];
        float maxY = pts[1];
        for (int i = 1; i < 4; i++) {
            minX = Math.min(minX, pts[i * 2]);
            minY = Math.min(minY, pts[i * 2 + 1]);
            maxX = Math.max(maxX, pts[i * 2]);
            maxY = Math.max(maxY, pts[i * 2 + 1]);
        }
        return new android.graphics.RectF(minX, minY, maxX, maxY);
    }

    public opendoja.compat.awt.geom.AffineTransform getTransform() {
        float[] v = new float[9];
        matrix.getValues(v);
        return new opendoja.compat.awt.geom.AffineTransform(v[0], v[3], v[1], v[4], v[2], v[5]);
    }

    public void setTransform(opendoja.compat.awt.geom.AffineTransform p0) {
        if (!ready() || p0 == null) {
            return;
        }
        double[] m = new double[6];
        p0.getMatrix(m);
        matrix.setValues(new float[]{(float) m[0], (float) m[2], (float) m[4],
                (float) m[1], (float) m[3], (float) m[5], 0f, 0f, 1f});
        stateDirty = true;
    }

    public void transform(opendoja.compat.awt.geom.AffineTransform p0) {
        if (!ready() || p0 == null) {
            return;
        }
        double[] m = new double[6];
        p0.getMatrix(m);
        android.graphics.Matrix tx = new android.graphics.Matrix();
        tx.setValues(new float[]{(float) m[0], (float) m[2], (float) m[4],
                (float) m[1], (float) m[3], (float) m[5], 0f, 0f, 1f});
        matrix.postConcat(tx);
        stateDirty = true;
    }

    public void translate(int p0, int p1) {
        if (!ready()) {
            return;
        }
        matrix.postTranslate(p0, p1);
        stateDirty = true;
    }

    public void scale(double p0, double p1) {
        if (!ready()) {
            return;
        }
        matrix.postScale((float) p0, (float) p1);
        stateDirty = true;
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.STROKE);
        if (p == null) {
            return;
        }
        canvas.drawLine(x1, y1, x2, y2, p);
    }

    public void drawRect(int x, int y, int w, int h) {
        if (w < 0) {
            x += w;
            w = -w;
        }
        if (h < 0) {
            y += h;
            h = -h;
        }
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.STROKE);
        if (p == null) {
            return;
        }
        canvas.drawRect(x, y, x + w, y + h, p);
    }

    public void fillRect(int x, int y, int w, int h) {
        if (w < 0) {
            x += w;
            w = -w;
        }
        if (h < 0) {
            y += h;
            h = -h;
        }
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.FILL);
        if (p == null) {
            logProbe("fillRect skipped not-ready x=" + x + " y=" + y);
            return;
        }
        canvas.drawRect(x, y, x + w, y + h, p);
        if (probeCount < 3) {
            int px = target.getBitmap().getPixel(x, y);
            android.graphics.Matrix m = new android.graphics.Matrix();
            canvas.getMatrix(m);
            float[] mv = new float[9];
            m.getValues(mv);
            logProbe("fillRect#" + probeCount + " xywh=" + x + "," + y + "," + w + "," + h
                    + " color=0x" + Integer.toHexString(color.getRGB())
                    + " px=" + Integer.toHexString(px)
                    + " clip=" + clipDevice
                    + " mat=" + java.util.Arrays.toString(mv)
                    + " target=" + System.identityHashCode(target.getBitmap()));
            probeCount++;
        }
    }

    private static int probeCount;

    private static void logProbe(String message) {
        if (probeCount <= 3) {
            android.util.Log.i("OpenDoJa", "G2D " + message);
        }
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight) {
        if (w < 0) {
            x += w;
            w = -w;
        }
        if (h < 0) {
            y += h;
            h = -h;
        }
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.FILL);
        if (p == null) {
            return;
        }
        canvas.drawRoundRect(x, y, x + w, y + h, Math.abs(arcWidth) / 2f, Math.abs(arcHeight) / 2f, p);
    }

    public void drawPolyline(int[] xs, int[] ys, int n) {
        if (xs == null || ys == null || n < 2) {
            return;
        }
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.STROKE);
        if (p == null) {
            return;
        }
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) {
            path.lineTo(xs[i], ys[i]);
        }
        canvas.drawPath(path, p);
    }

    public void fillPolygon(int[] xs, int[] ys, int n) {
        if (xs == null || ys == null || n < 3) {
            return;
        }
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.FILL);
        if (p == null) {
            return;
        }
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) {
            path.lineTo(xs[i], ys[i]);
        }
        path.close();
        canvas.drawPath(path, p);
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.STROKE);
        if (p == null) {
            return;
        }
        canvas.drawArc(x, y, x + w, y + h, -startAngle, -arcAngle, false, p);
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.FILL);
        if (p == null) {
            return;
        }
        canvas.drawArc(x, y, x + w, y + h, -startAngle, -arcAngle, true, p);
    }

    public void drawString(java.lang.String text, int x, int y) {
        if (text == null || text.isEmpty() || !ready()) {
            return;
        }
        syncState();
        paint.setStyle(android.graphics.Paint.Style.FILL);
        boolean previousAA = paint.isAntiAlias();
        if (textAntiAlias != previousAA) {
            paint.setAntiAlias(textAntiAlias);
        }
        try {
            canvas.drawText(text, x, y, paint);
        } finally {
            if (paint.isAntiAlias() != previousAA) {
                paint.setAntiAlias(previousAA);
            }
        }
    }

    public void draw(opendoja.compat.awt.Shape p0) {
        if (p0 instanceof opendoja.compat.awt.Rectangle rect) {
            drawRect(rect.x, rect.y, rect.width, rect.height);
        }
    }

    private static android.graphics.Bitmap bitmapOf(opendoja.compat.awt.Image img) {
        if (img instanceof opendoja.compat.awt.image.BufferedImage bi) {
            return bi.getBitmap();
        }
        return null;
    }

    public boolean drawImage(opendoja.compat.awt.Image img, int x, int y, opendoja.compat.awt.image.ImageObserver observer) {
        android.graphics.Bitmap bmp = bitmapOf(img);
        if (bmp == null || bmp.isRecycled() || !ready()) {
            return false;
        }
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.FILL);
        canvas.drawBitmap(bmp, x, y, p);
        return true;
    }

    public boolean drawImage(opendoja.compat.awt.Image img, int x, int y, int w, int h, opendoja.compat.awt.image.ImageObserver observer) {
        android.graphics.Bitmap bmp = bitmapOf(img);
        if (bmp == null || bmp.isRecycled() || !ready() || w <= 0 || h <= 0) {
            return false;
        }
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.FILL);
        if (bmp.getWidth() == w && bmp.getHeight() == h) {
            canvas.drawBitmap(bmp, x, y, p);
        } else {
            canvas.drawBitmap(bmp, null, new android.graphics.RectF(x, y, x + w, y + h), p);
        }
        return true;
    }

    public boolean drawImage(opendoja.compat.awt.Image img, int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2, opendoja.compat.awt.image.ImageObserver observer) {
        android.graphics.Bitmap bmp = bitmapOf(img);
        if (bmp == null || bmp.isRecycled() || !ready()) {
            return false;
        }
        int sw = sx2 - sx1;
        int sh = sy2 - sy1;
        if (sw <= 0 || sh <= 0) {
            return false;
        }
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.FILL);
        canvas.drawBitmap(bmp, new android.graphics.Rect(sx1, sy1, sx2, sy2),
                new android.graphics.RectF(dx1, dy1, dx2, dy2), p);
        return true;
    }

    public boolean drawImage(opendoja.compat.awt.Image img, opendoja.compat.awt.geom.AffineTransform xform,
                             opendoja.compat.awt.image.ImageObserver observer) {
        android.graphics.Bitmap bmp = bitmapOf(img);
        if (bmp == null || bmp.isRecycled() || !ready()) {
            return false;
        }
        android.graphics.Paint p = prepare(android.graphics.Paint.Style.FILL);
        canvas.save();
        try {
            if (xform != null) {
                double[] m = new double[6];
                xform.getMatrix(m);
                android.graphics.Matrix tx = new android.graphics.Matrix();
                tx.setValues(new float[]{(float) m[0], (float) m[2], (float) m[4],
                        (float) m[1], (float) m[3], (float) m[5], 0f, 0f, 1f});
                canvas.concat(tx);
            }
            canvas.drawBitmap(bmp, 0f, 0f, p);
        } finally {
            canvas.restore();
        }
        return true;
    }

    public opendoja.compat.awt.Graphics create() {
        if (!ready()) {
            return null;
        }
        Graphics2D child = new Graphics2D(target);
        child.color = color;
        child.font = font;
        child.composite = composite;
        child.hints = new opendoja.compat.awt.RenderingHints(hints);
        child.antiAlias = antiAlias;
        child.textAntiAlias = textAntiAlias;
        child.filterBitmap = filterBitmap;
        child.matrix.set(matrix);
        child.clipDevice = clipDevice == null ? null : new android.graphics.RectF(clipDevice);
        child.paint.setTextSize(font.getSize2D());
        child.paint.setFilterBitmap(filterBitmap);
        child.applyColorAndComposite();
        child.stateDirty = true;
        return child;
    }

    public void dispose() {
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }
}
