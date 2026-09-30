package opendoja.probes;

import opendoja.host.DesktopSurface;

import java.awt.image.BufferedImage;

public final class DesktopSurfacePresentationProbe {
    private static final int RED = 0xFFFF0000;
    private static final int GREEN = 0xFF00FF00;
    private static final int BLUE = 0xFF0000FF;

    private DesktopSurfacePresentationProbe() {
    }

    public static void main(String[] args) {
        DesktopSurface surface = new DesktopSurface(2, 2);

        fill(surface.image(), RED);
        BufferedImage first = surface.copyForPresentation();

        fill(surface.image(), GREEN);
        BufferedImage second = surface.copyForPresentation();

        fill(surface.image(), BLUE);
        BufferedImage third = surface.copyForPresentation();

        checkSolid(first, RED, "first presented frame should remain stable after later snapshots");
        checkSolid(second, GREEN, "second presented frame should remain stable after later snapshots");
        checkSolid(third, BLUE, "third presented frame should match the latest snapshot");

        System.out.println("Desktop surface presentation probe OK");
    }

    private static void fill(BufferedImage image, int argb) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, argb);
            }
        }
    }

    private static void checkSolid(BufferedImage image, int expectedArgb, String message) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != expectedArgb) {
                    throw new IllegalStateException(message + ": got 0x"
                            + Integer.toHexString(image.getRGB(x, y))
                            + " at " + x + "," + y);
                }
            }
        }
    }
}
