package opendoja.compat.awt;

public interface Shape {
    default opendoja.compat.awt.Rectangle getBounds()  { return null; }
}

