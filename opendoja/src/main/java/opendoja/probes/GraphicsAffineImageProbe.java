package opendoja.probes;

import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;

public final class GraphicsAffineImageProbe {
    private static final int RED = 0xFFFF0000;
    private static final int GREEN = 0xFF00FF00;
    private static final int BLUE = 0xFF0000FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private GraphicsAffineImageProbe() {
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        verifyTranslation();
        verifyScaling();
        verifySourceClippingKeepsLocalCoordinates();
        verifyTrailingMatrixElementsAreIgnored();
        verifyMatrixValidation();

        System.out.println("Graphics affine image probe OK");
    }

    private static void verifyTranslation() {
        Image source = Image.createImage(2, 1, new int[]{RED, BLUE}, 0);
        Image target = Image.createImage(5, 4);
        Graphics graphics = target.getGraphics();
        graphics.setRGBPixels(0, 0, 5, 4, fill(5 * 4, WHITE), 0);

        graphics.drawImage(source, affineMatrix(1, 0, 1, 0, 1, 2));

        assertPixel("translation leaves untouched origin", graphics, 0, 0, WHITE);
        assertPixel("translation draws first pixel at translated x/y", graphics, 1, 2, RED);
        assertPixel("translation draws second pixel at translated x/y", graphics, 2, 2, BLUE);
    }

    private static void verifyScaling() {
        Image source = Image.createImage(2, 1, new int[]{RED, BLUE}, 0);
        Image target = Image.createImage(5, 3);
        Graphics graphics = target.getGraphics();
        graphics.setRGBPixels(0, 0, 5, 3, fill(5 * 3, BLACK), 0);

        graphics.drawImage(source, affineMatrix(2, 0, 0, 0, 1, 0));

        assertPixel("scaled first source pixel covers first destination pixel", graphics, 0, 0, RED);
        assertPixel("scaled first source pixel covers second destination pixel", graphics, 1, 0, RED);
        assertPixel("scaled second source pixel covers third destination pixel", graphics, 2, 0, BLUE);
        assertPixel("scaled second source pixel covers fourth destination pixel", graphics, 3, 0, BLUE);
        assertPixel("scaled draw leaves remaining area unchanged", graphics, 4, 0, BLACK);
    }

    private static void verifySourceClippingKeepsLocalCoordinates() {
        Image source = Image.createImage(3, 1, new int[]{RED, GREEN, BLUE}, 0);
        Image target = Image.createImage(4, 2);
        Graphics graphics = target.getGraphics();
        graphics.setRGBPixels(0, 0, 4, 2, fill(4 * 2, WHITE), 0);

        graphics.drawImage(source, affineMatrix(1, 0, 0, 0, 1, 0), -1, 0, 3, 1);

        assertPixel("clipped source keeps skipped column empty", graphics, 0, 0, WHITE);
        assertPixel("clipped source keeps first valid pixel at local x=1", graphics, 1, 0, RED);
        assertPixel("clipped source keeps second valid pixel at local x=2", graphics, 2, 0, GREEN);
        assertPixel("clipped source does not invent pixels beyond the image", graphics, 3, 0, WHITE);
    }

    private static void verifyTrailingMatrixElementsAreIgnored() {
        Image source = Image.createImage(1, 1, new int[]{BLUE}, 0);
        Image target = Image.createImage(3, 2);
        Graphics graphics = target.getGraphics();
        graphics.setRGBPixels(0, 0, 3, 2, fill(3 * 2, WHITE), 0);

        graphics.drawImage(source, new int[]{4096, 0, 4096, 0, 4096, 0, 123456789});

        assertPixel("trailing matrix elements do not affect the draw", graphics, 1, 0, BLUE);
        assertPixel("trailing matrix elements do not move the origin", graphics, 0, 0, WHITE);
    }

    private static void verifyMatrixValidation() {
        Image source = Image.createImage(1, 1, new int[]{RED}, 0);
        Graphics graphics = Image.createImage(1, 1).getGraphics();

        assertThrows("null image", NullPointerException.class, () -> graphics.drawImage(null, affineMatrix(1, 0, 0, 0, 1, 0)));
        assertThrows("null matrix", NullPointerException.class, () -> graphics.drawImage(source, null));
        assertThrows("short matrix", ArrayIndexOutOfBoundsException.class, () -> graphics.drawImage(source, new int[5]));
        assertThrows("zero width", IllegalArgumentException.class,
                () -> graphics.drawImage(source, affineMatrix(1, 0, 0, 0, 1, 0), 0, 0, 0, 1));
        assertThrows("zero height", IllegalArgumentException.class,
                () -> graphics.drawImage(source, affineMatrix(1, 0, 0, 0, 1, 0), 0, 0, 1, 0));
    }

    private static int[] affineMatrix(int m00, int m01, int m02, int m10, int m11, int m12) {
        return new int[]{
                m00 * 4096,
                m01 * 4096,
                m02 * 4096,
                m10 * 4096,
                m11 * 4096,
                m12 * 4096
        };
    }

    private static int[] fill(int length, int value) {
        int[] pixels = new int[length];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = value;
        }
        return pixels;
    }

    private static void assertPixel(String label, Graphics graphics, int x, int y, int expected) {
        int actual = graphics.getRGBPixel(x, y);
        if (actual != expected) {
            throw new IllegalStateException(label + " expected=0x" + Integer.toHexString(expected)
                    + " actual=0x" + Integer.toHexString(actual));
        }
    }

    private static void assertThrows(String label, Class<? extends Throwable> expected, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (expected.isInstance(throwable)) {
                return;
            }
            throw new IllegalStateException(label + " expected=" + expected.getName()
                    + " actual=" + throwable.getClass().getName(), throwable);
        }
        throw new IllegalStateException(label + " expected exception " + expected.getName());
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
