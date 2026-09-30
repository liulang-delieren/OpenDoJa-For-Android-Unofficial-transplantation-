package opendoja.probes;

import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;

public final class GraphicsRgbPixelProbe {
    private GraphicsRgbPixelProbe() {
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");

        Image image = Image.createImage(2, 2);
        Graphics graphics = image.getGraphics();

        graphics.setRGBPixel(0, 0, 0x00010203);
        assertPixel("single RGB write ignores top byte", graphics.getRGBPixel(0, 0), 0xFF010203);

        graphics.setRGBPixel(-1, 0, 0x00ABCDEF);
        graphics.setRGBPixel(2, 1, 0x00ABCDEF);
        graphics.setRGBPixel(1, 2, 0x00ABCDEF);
        assertPixel("off-surface RGB writes are ignored", graphics.getRGBPixel(0, 0), 0xFF010203);

        graphics.setRGBPixels(0, 1, 2, 1, new int[]{0x000A0B0C, 0x7F0D0E0F}, 0);
        assertPixel("bulk RGB write treats zero alpha byte as opaque", graphics.getRGBPixel(0, 1), 0xFF0A0B0C);
        assertPixel("bulk RGB write ignores non-zero top byte", graphics.getRGBPixel(1, 1), 0xFF0D0E0F);

        int clippedBaseline = graphics.getRGBPixel(1, 0);
        graphics.setClip(0, 0, 1, 1);
        graphics.setRGBPixel(1, 0, 0x00112233);
        assertPixel("clipped RGB writes are ignored", graphics.getRGBPixel(1, 0), clippedBaseline);

        assertPixel("off-surface getPixel returns the documented fallback color", graphics.getPixel(3, 3),
                Graphics.getColorOfName(Graphics.BLACK));
        assertPixel("off-surface getRGBPixel returns 0", graphics.getRGBPixel(3, 3), 0);
        int[] outOfBoundsPixels = graphics.getRGBPixels(1, 1, 2, 2, null, 0);
        assertPixel("getRGBPixels should preserve the in-surface source pixel", outOfBoundsPixels[0], 0xFF0D0E0F);
        assertPixel("getRGBPixels should zero any off-surface columns", outOfBoundsPixels[1], 0);
        assertPixel("getRGBPixels should zero any off-surface rows", outOfBoundsPixels[2], 0);
        assertPixel("getRGBPixels should zero any fully off-surface cells", outOfBoundsPixels[3], 0);

        Image fromPixels = Image.createImage(2, 1, new int[]{0x00112233, 0x7F445566}, 0);
        assertPixel("RGB image creation treats zero alpha byte as opaque",
                fromPixels.getGraphics().getRGBPixel(0, 0), 0xFF112233);

        System.out.println("Graphics RGB pixel probe OK");
    }

    private static void assertPixel(String label, int actual, int expected) {
        if (actual != expected) {
            throw new IllegalStateException(label + " expected=0x" + Integer.toHexString(expected)
                    + " actual=0x" + Integer.toHexString(actual));
        }
    }
}
