package opendoja.compat.awt.geom;

public class AffineTransform implements java.lang.Cloneable, java.io.Serializable {
    public static final int TYPE_IDENTITY = 0;
    public static final int TYPE_TRANSLATION = 1;
    public static final int TYPE_UNIFORM_SCALE = 2;
    public static final int TYPE_GENERAL_SCALE = 4;
    public static final int TYPE_MASK_SCALE = 6;
    public static final int TYPE_FLIP = 64;
    public static final int TYPE_QUADRANT_ROTATION = 8;
    public static final int TYPE_GENERAL_ROTATION = 16;
    public static final int TYPE_MASK_ROTATION = 24;
    public static final int TYPE_GENERAL_TRANSFORM = 32;

    private double m00;
    private double m10;
    private double m01;
    private double m11;
    private double m02;
    private double m12;

    public AffineTransform() {
        m00 = 1.0;
        m11 = 1.0;
    }

    public AffineTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
    }

    public AffineTransform(opendoja.compat.awt.geom.AffineTransform tx) {
        setMatrix(tx.m00, tx.m10, tx.m01, tx.m11, tx.m02, tx.m12);
    }

    public static opendoja.compat.awt.geom.AffineTransform getTranslateInstance(double tx, double ty) {
        return new AffineTransform(1.0, 0.0, 0.0, 1.0, tx, ty);
    }

    public static opendoja.compat.awt.geom.AffineTransform getScaleInstance(double sx, double sy) {
        return new AffineTransform(sx, 0.0, 0.0, sy, 0.0, 0.0);
    }

    public static opendoja.compat.awt.geom.AffineTransform getRotateInstance(double theta) {
        AffineTransform tx = new AffineTransform();
        tx.rotate(theta);
        return tx;
    }

    public static opendoja.compat.awt.geom.AffineTransform getRotateInstance(double theta, double x, double y) {
        AffineTransform tx = new AffineTransform();
        tx.rotate(theta, x, y);
        return tx;
    }

    private void setMatrix(double m00, double m10, double m01, double m11, double m02, double m12) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
    }

    public void translate(double tx, double ty) {
        m02 += (m00 * tx) + (m01 * ty);
        m12 += (m10 * tx) + (m11 * ty);
    }

    public void scale(double sx, double sy) {
        m00 *= sx;
        m10 *= sx;
        m01 *= sy;
        m11 *= sy;
    }

    public void rotate(double theta) {
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        double n00 = (m00 * cos) + (m01 * sin);
        double n01 = (m00 * -sin) + (m01 * cos);
        double n10 = (m10 * cos) + (m11 * sin);
        double n11 = (m10 * -sin) + (m11 * cos);
        m00 = n00;
        m01 = n01;
        m10 = n10;
        m11 = n11;
    }

    public void rotate(double theta, double x, double y) {
        translate(x, y);
        rotate(theta);
        translate(-x, -y);
    }

    public void shear(double shx, double shy) {
        AffineTransform shear = new AffineTransform(1.0, shy, shx, 1.0, 0.0, 0.0);
        concatenate(shear);
    }

    public void concatenate(opendoja.compat.awt.geom.AffineTransform tx) {
        // this = this x tx (post-concatenate, matching java.awt order)
        double n00 = (m00 * tx.m00) + (m01 * tx.m10);
        double n10 = (m10 * tx.m00) + (m11 * tx.m10);
        double n01 = (m00 * tx.m01) + (m01 * tx.m11);
        double n11 = (m10 * tx.m01) + (m11 * tx.m11);
        double n02 = (m00 * tx.m02) + (m01 * tx.m12) + m02;
        double n12 = (m10 * tx.m02) + (m11 * tx.m12) + m12;
        setMatrix(n00, n10, n01, n11, n02, n12);
    }

    public void preConcatenate(opendoja.compat.awt.geom.AffineTransform tx) {
        // this = tx x this (pre-concatenate, matching java.awt order)
        double n00 = (tx.m00 * m00) + (tx.m01 * m10);
        double n10 = (tx.m10 * m00) + (tx.m11 * m10);
        double n01 = (tx.m00 * m01) + (tx.m01 * m11);
        double n11 = (tx.m10 * m01) + (tx.m11 * m11);
        double n02 = (tx.m00 * m02) + (tx.m01 * m12) + tx.m02;
        double n12 = (tx.m10 * m02) + (tx.m11 * m12) + tx.m12;
        setMatrix(n00, n10, n01, n11, n02, n12);
    }

    public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        if (dstPts == srcPts) {
            for (int i = 0; i < numPts; i++) {
                double x = srcPts[srcOff + (i * 2)];
                double y = srcPts[srcOff + (i * 2) + 1];
                dstPts[dstOff + (i * 2)] = (m00 * x) + (m01 * y) + m02;
                dstPts[dstOff + (i * 2) + 1] = (m10 * x) + (m11 * y) + m12;
            }
            return;
        }
        for (int i = 0; i < numPts; i++) {
            double x = srcPts[srcOff + (i * 2)];
            double y = srcPts[srcOff + (i * 2) + 1];
            dstPts[dstOff + (i * 2)] = (m00 * x) + (m01 * y) + m02;
            dstPts[dstOff + (i * 2) + 1] = (m10 * x) + (m11 * y) + m12;
        }
    }

    public opendoja.compat.awt.geom.Point2D transform(opendoja.compat.awt.geom.Point2D pt, opendoja.compat.awt.geom.Point2D ptDst) {
        double x = pt.getX();
        double y = pt.getY();
        double nx = (m00 * x) + (m01 * y) + m02;
        double ny = (m10 * x) + (m11 * y) + m12;
        if (ptDst == null) {
            return new opendoja.compat.awt.geom.Point2D.Double(nx, ny);
        }
        ptDst.setLocation(nx, ny);
        return ptDst;
    }

    public opendoja.compat.awt.geom.Point2D inverseTransform(opendoja.compat.awt.geom.Point2D pt, opendoja.compat.awt.geom.Point2D ptDst)
            throws opendoja.compat.awt.geom.NoninvertibleTransformException {
        double det = (m00 * m11) - (m01 * m10);
        if (det == 0.0 || Double.isNaN(det) || Double.isInfinite(det)) {
            throw new opendoja.compat.awt.geom.NoninvertibleTransformException();
        }
        double x = pt.getX();
        double y = pt.getY();
        double nx = (((x - m02) * m11) - ((y - m12) * m01)) / det;
        double ny = (((y - m12) * m00) - ((x - m02) * m10)) / det;
        if (ptDst == null) {
            return new opendoja.compat.awt.geom.Point2D.Double(nx, ny);
        }
        ptDst.setLocation(nx, ny);
        return ptDst;
    }

    public void invert() throws opendoja.compat.awt.geom.NoninvertibleTransformException {
        double det = (m00 * m11) - (m01 * m10);
        if (det == 0.0 || Double.isNaN(det) || Double.isInfinite(det)) {
            throw new opendoja.compat.awt.geom.NoninvertibleTransformException();
        }
        double n00 = m11 / det;
        double n10 = -m10 / det;
        double n01 = -m01 / det;
        double n11 = m00 / det;
        double n02 = ((m01 * m12) - (m11 * m02)) / det;
        double n12 = ((m10 * m02) - (m00 * m12)) / det;
        setMatrix(n00, n10, n01, n11, n02, n12);
    }

    public double determinant() {
        return (m00 * m11) - (m01 * m10);
    }

    public boolean isIdentity() {
        return m00 == 1.0 && m11 == 1.0 && m01 == 0.0 && m10 == 0.0 && m02 == 0.0 && m12 == 0.0;
    }

    public void getMatrix(double[] matrix) {
        matrix[0] = m00;
        matrix[1] = m10;
        matrix[2] = m01;
        matrix[3] = m11;
        matrix[4] = m02;
        matrix[5] = m12;
    }

    public void setToIdentity() {
        m00 = 1.0;
        m11 = 1.0;
        m01 = 0.0;
        m10 = 0.0;
        m02 = 0.0;
        m12 = 0.0;
    }

    public double getTranslateX() {
        return m02;
    }

    public double getTranslateY() {
        return m12;
    }

    public double getScaleX() {
        return m00;
    }

    public double getScaleY() {
        return m11;
    }

    public opendoja.compat.awt.Shape createTransformedShape(opendoja.compat.awt.Shape p0) {
        if (p0 == null) {
            return null;
        }
        if (p0 instanceof opendoja.compat.awt.Rectangle rect) {
            double[] pts = {rect.x, rect.y, rect.x + rect.width, rect.y, rect.x + rect.width,
                    rect.y + rect.height, rect.x, rect.y + rect.height};
            transform(pts, 0, pts, 0, 4);
            double minX = pts[0];
            double minY = pts[1];
            double maxX = pts[0];
            double maxY = pts[1];
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
        opendoja.compat.awt.Rectangle bounds = p0.getBounds();
        if (bounds == null) {
            return null;
        }
        return createTransformedShape(bounds);
    }

    public int getType() {
        if (m01 == 0.0 && m10 == 0.0 && m00 == 1.0 && m11 == 1.0) {
            return (m02 == 0.0 && m12 == 0.0) ? TYPE_IDENTITY : TYPE_TRANSLATION;
        }
        return TYPE_GENERAL_TRANSFORM;
    }

    @Override
    public Object clone() {
        return new AffineTransform(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AffineTransform other)) {
            return false;
        }
        return m00 == other.m00 && m10 == other.m10 && m01 == other.m01
                && m11 == other.m11 && m02 == other.m02 && m12 == other.m12;
    }

    @Override
    public int hashCode() {
        long bits = 1;
        bits = (31 * bits) + Double.doubleToLongBits(m00);
        bits = (31 * bits) + Double.doubleToLongBits(m10);
        bits = (31 * bits) + Double.doubleToLongBits(m01);
        bits = (31 * bits) + Double.doubleToLongBits(m11);
        bits = (31 * bits) + Double.doubleToLongBits(m02);
        bits = (31 * bits) + Double.doubleToLongBits(m12);
        return (int) (bits ^ (bits >>> 32));
    }

    @Override
    public String toString() {
        return "opendoja.compat.awt.geom.AffineTransform[[" + m00 + "," + m01 + "," + m02 + "],["
                + m10 + "," + m11 + "," + m12 + "]]";
    }
}
