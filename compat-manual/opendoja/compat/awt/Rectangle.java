package opendoja.compat.awt;

public class Rectangle extends opendoja.compat.awt.geom.Rectangle2D implements opendoja.compat.awt.Shape, java.io.Serializable {
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;

    public Rectangle() {
    }

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(opendoja.compat.awt.Rectangle r) {
        this(r.x, r.y, r.width, r.height);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public opendoja.compat.awt.Rectangle getBounds() {
        return new opendoja.compat.awt.Rectangle(x, y, width, height);
    }

    public boolean isEmpty() {
        return width <= 0 || height <= 0;
    }

    public boolean contains(int px, int py) {
        int w = width;
        int h = height;
        if (w <= 0 || h <= 0) {
            return false;
        }
        if (px < x || py < y) {
            return false;
        }
        return px < x + w && py < y + h;
    }

    public boolean contains(double px, double py) {
        if (px < x || py < y || px >= x + width || py >= y + height) {
            return false;
        }
        return width > 0 && height > 0;
    }

    public boolean intersects(opendoja.compat.awt.Rectangle r) {
        if (r == null || isEmpty() || r.isEmpty()) {
            return false;
        }
        int x1 = Math.max(x, r.x);
        int y1 = Math.max(y, r.y);
        int x2 = Math.min(x + width, r.x + r.width);
        int y2 = Math.min(y + height, r.y + r.height);
        return x1 < x2 && y1 < y2;
    }

    public opendoja.compat.awt.Rectangle intersection(opendoja.compat.awt.Rectangle r) {
        int x1 = Math.max(x, r.x);
        int y1 = Math.max(y, r.y);
        int x2 = Math.min(x + width, r.x + r.width);
        int y2 = Math.min(y + height, r.y + r.height);
        int w = x2 - x1;
        int h = y2 - y1;
        if (w <= 0 || h <= 0) {
            return new opendoja.compat.awt.Rectangle();
        }
        return new opendoja.compat.awt.Rectangle(x1, y1, w, h);
    }

    public opendoja.compat.awt.Rectangle union(opendoja.compat.awt.Rectangle r) {
        if (r == null || r.isEmpty()) {
            return getBounds();
        }
        if (isEmpty()) {
            return r.getBounds();
        }
        int x1 = Math.min(x, r.x);
        int y1 = Math.min(y, r.y);
        int x2 = Math.max(x + width, r.x + r.width);
        int y2 = Math.max(y + height, r.y + r.height);
        return new opendoja.compat.awt.Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    public void setRect(double rx, double ry, double rw, double rh) {
        int nx = (int) Math.floor(rx);
        int ny = (int) Math.floor(ry);
        x = nx;
        y = ny;
        width = (int) Math.ceil(rx + rw) - nx;
        height = (int) Math.ceil(ry + rh) - ny;
    }

    public void setLocation(int nx, int ny) {
        this.x = nx;
        this.y = ny;
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void setSize(int w, int h) {
        this.width = w;
        this.height = h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Rectangle other)) {
            return false;
        }
        return x == other.x && y == other.y && width == other.width && height == other.height;
    }

    @Override
    public int hashCode() {
        long bits = (((x * 31L + y) * 31L + width) * 31L) + height;
        return (int) (bits ^ (bits >>> 32));
    }

    @Override
    public String toString() {
        return "opendoja.compat.awt.Rectangle[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
    }
}
