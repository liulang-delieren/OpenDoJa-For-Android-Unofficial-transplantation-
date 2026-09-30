package opendoja.compat.awt;

public class GraphicsEnvironment {
    private static final GraphicsEnvironment INSTANCE = new GraphicsEnvironment();

    public GraphicsEnvironment() {
    }

    public static opendoja.compat.awt.GraphicsEnvironment getLocalGraphicsEnvironment() {
        return INSTANCE;
    }

    public java.lang.String[] getAvailableFontFamilyNames() {
        return new String[]{"Dialog", "SansSerif", "Serif", "Monospaced"};
    }

    public static boolean isHeadless() {
        return true;
    }

    public opendoja.compat.awt.Rectangle getMaximumWindowBounds() throws opendoja.compat.awt.HeadlessException {
        return new opendoja.compat.awt.Rectangle(0, 0, 1080, 1920);
    }

    public opendoja.compat.awt.GraphicsDevice getDefaultScreenDevice() throws opendoja.compat.awt.HeadlessException {
        return null;
    }
}
