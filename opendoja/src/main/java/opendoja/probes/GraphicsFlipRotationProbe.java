package opendoja.probes;

import com.nttdocomo.ui.*;
import opendoja.host.DesktopLauncher;

import java.awt.image.BufferedImage;

public final class GraphicsFlipRotationProbe {
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private GraphicsFlipRotationProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ProbeApp app = (ProbeApp) DesktopLauncher.launch(ProbeApp.class);
        try {
            app.runProbe();
        } finally {
            app.terminate();
        }
    }

    public static final class ProbeApp extends IApplication {
        private final ProbeCanvas canvas = new ProbeCanvas();

        @Override
        public void start() {
            Display.setCurrent((Frame) canvas);
        }

        void runProbe() {
            canvas.verifyNonSquareRightAngleFlip();
        }
    }

    static final class ProbeCanvas extends com.nttdocomo.ui.Canvas {
        @Override
        public void paint(Graphics g) {
            return;
        }

        void verifyNonSquareRightAngleFlip() {
            Graphics g = getGraphics();
            try {
                Image strip = solid(240, 95, WHITE);

                clearAndDraw(g, strip, Graphics.FLIP_ROTATE_RIGHT, 145, 0);
                BufferedImage right = snapshot();
                assertColor(right.getRGB(144, 10), BLACK, "right-rotated strip should not spill left");
                assertColor(right.getRGB(150, 10), WHITE, "right-rotated strip top-left");
                assertColor(right.getRGB(238, 230), WHITE, "right-rotated strip bottom-right");

                clearAndDraw(g, strip, Graphics.FLIP_ROTATE_LEFT, 0, 0);
                BufferedImage left = snapshot();
                assertColor(left.getRGB(95, 10), BLACK, "left-rotated strip should not spill right");
                assertColor(left.getRGB(10, 10), WHITE, "left-rotated strip top-left");
                assertColor(left.getRGB(90, 230), WHITE, "left-rotated strip bottom-right");

                System.out.println("Graphics flip rotation probe OK");
            } finally {
                g.dispose();
            }
        }

        private static void clearAndDraw(Graphics g, Image strip, int flipMode, int x, int y) {
            g.lock();
            try {
                g.setFlipMode(Graphics.FLIP_NONE);
                g.setColor(BLACK);
                g.fillRect(0, 0, Display.getWidth(), Display.getHeight());
                g.setFlipMode(flipMode);
                g.drawImage(strip, x, y);
            } finally {
                g.setFlipMode(Graphics.FLIP_NONE);
                g.unlock(true);
            }
        }

        private static Image solid(int width, int height, int argb) {
            Image image = Image.createImage(width, height);
            Graphics g = image.getGraphics();
            try {
                g.setColor(argb);
                g.fillRect(0, 0, width, height);
                return image;
            } finally {
                g.dispose();
            }
        }

        private BufferedImage snapshot() {
            Graphics graphics = getGraphics();
            try {
                java.lang.reflect.Field surfaceField = Graphics.class.getDeclaredField("surface");
                surfaceField.setAccessible(true);
                Object surface = surfaceField.get(graphics);
                java.lang.reflect.Method imageMethod = surface.getClass().getDeclaredMethod("image");
                imageMethod.setAccessible(true);
                return (BufferedImage) imageMethod.invoke(surface);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to read canvas frame", e);
            } finally {
                graphics.dispose();
            }
        }

        private static void assertColor(int actual, int expected, String message) {
            if (actual != expected) {
                throw new IllegalStateException(message + " actual=0x" + Integer.toHexString(actual)
                        + " expected=0x" + Integer.toHexString(expected));
            }
        }
    }
}
