package opendoja.probes;

import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.MediaManager;
import com.nttdocomo.ui._ImageInternalAccess;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PacManiaTextureReadbackProbe {
    private static final Path GAME_JAR = Path.of("resources/sample_games/PACMANIA/pacmania_N906i.jar");
    private static final String OPAQUE_ENTRY = "pm_chr_pm01.gif";

    private PacManiaTextureReadbackProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        verifyEntry(OpaqueExpectation.OPAQUE_ONLY, OPAQUE_ENTRY);
        verifyTransparentEntry();

        System.out.println("PACMANIA texture readback probe OK");
    }

    private static void verifyEntry(OpaqueExpectation expectation, String entryName) throws IOException {
        byte[] data = readEntry(entryName);
        BufferedImage reference = ImageIO.read(new ByteArrayInputStream(data));
        check(reference != null, "ImageIO failed to decode " + entryName);

        Image image = MediaManager.getImage(data).getImage();
        int[] actualPixels = _ImageInternalAccess.copyDisplayPixels(image);
        check(actualPixels.length == reference.getWidth() * reference.getHeight(),
                "pixel readback length mismatch for " + entryName);

        SamplePoint opaque = findOpaqueSample(reference);
        check(opaque != null, "expected at least one opaque texture pixel in " + entryName);
        int actualOpaque = actualPixels[(opaque.y() * reference.getWidth()) + opaque.x()];
        check(actualOpaque == opaque.argb(),
                String.format("%s opaque texture pixel mismatch at (%d,%d): expected %08x got %08x",
                        entryName, opaque.x(), opaque.y(), opaque.argb(), actualOpaque));

        SamplePoint transparent = findTransparentSample(reference);
        if (expectation == OpaqueExpectation.REQUIRES_TRANSPARENCY) {
            check(transparent != null, "expected transparent texture pixel in " + entryName);
        }
        if (transparent != null) {
            int actualTransparent = actualPixels[(transparent.y() * reference.getWidth()) + transparent.x()];
            check((actualTransparent >>> 24) == 0,
                    String.format("%s transparent texture pixel lost zero alpha at (%d,%d): expected %08x got %08x",
                            entryName, transparent.x(), transparent.y(), transparent.argb(), actualTransparent));
        }
    }

    private static byte[] readEntry(String entryName) throws IOException {
        try (ZipFile zip = new ZipFile(GAME_JAR.toFile())) {
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                throw new IOException("Missing jar entry: " + entryName);
            }
            return zip.getInputStream(entry).readAllBytes();
        }
    }

    private static void verifyTransparentEntry() throws IOException {
        try (ZipFile zip = new ZipFile(GAME_JAR.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".gif")) {
                    continue;
                }
                byte[] data = zip.getInputStream(entry).readAllBytes();
                BufferedImage reference = ImageIO.read(new ByteArrayInputStream(data));
                if (reference == null || findTransparentSample(reference) == null) {
                    continue;
                }
                verifyEntry(OpaqueExpectation.REQUIRES_TRANSPARENCY, name);
                return;
            }
        }
        throw new IllegalStateException("expected at least one transparent PACMANIA texture asset");
    }

    private static SamplePoint findOpaqueSample(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                return new SamplePoint(x, y, argb);
            }
        }
        return null;
    }

    private static SamplePoint findTransparentSample(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    return new SamplePoint(x, y, argb);
                }
            }
        }
        return null;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private record SamplePoint(int x, int y, int argb) {
    }

    private enum OpaqueExpectation {
        OPAQUE_ONLY,
        REQUIRES_TRANSPARENCY
    }
}
