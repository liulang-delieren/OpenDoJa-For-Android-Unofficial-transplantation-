package opendoja.compat.awt.geom;

public class Point2D implements java.lang.Cloneable {
    protected double x;
    protected double y;

    public Point2D() {
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setLocation(double nx, double ny) {
        this.x = nx;
        this.y = ny;
    }

    public void setLocation(opendoja.compat.awt.geom.Point2D p) {
        setLocation(p.x, p.y);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Double extends opendoja.compat.awt.geom.Point2D implements java.io.Serializable {
        public Double() {
        }

        public Double(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "opendoja.compat.awt.geom.Point2D.Double[x=" + x + ",y=" + y + "]";
        }
    }
}
