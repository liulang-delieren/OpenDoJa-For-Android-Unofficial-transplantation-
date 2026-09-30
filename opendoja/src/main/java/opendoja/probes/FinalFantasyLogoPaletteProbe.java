package opendoja.probes;

import com.nttdocomo.ui.Palette;
import com.nttdocomo.ui.PalettedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FinalFantasyLogoPaletteProbe {
    private static final Path GAME_JAR = Path.of(
            "resources/sample_games/Final_Fantasy_1_doja/N905i_Version/bin/Final_Fantasy_1.jar");
    private static final String ENTRY_NAME = "menu_ff1.gif";

    private FinalFantasyLogoPaletteProbe() {
    }

    public static void main(String[] args) throws Exception {
        byte[] data = readEntry();
        BufferedImage indexed = ImageIO.read(new java.io.ByteArrayInputStream(data));
        if (!(indexed.getColorModel() instanceof IndexColorModel colorModel)) {
            throw new IllegalStateException("Expected indexed GIF for " + ENTRY_NAME);
        }
        PalettedImage image = PalettedImage.createPalettedImage(data);
        Palette palette = image.getPalette();

        Sample sample = pickBestSample(indexed.getRaster(), indexed.getWidth(), indexed.getHeight(), colorModel, palette);
        if (sample.fixedError() > 24) {
            throw new IllegalStateException(String.format(
                    "FF1 palette entry still decodes incorrectly: index=%d original=%s actual=%s error=%d",
                    sample.index(),
                    formatRgb(sample.originalRgb()),
                    formatRgb(sample.actualRgb()),
                    sample.fixedError()));
        }
        if (sample.brokenError() <= sample.fixedError()) {
            throw new IllegalStateException(String.format(
                    "Probe no longer demonstrates the ARGB-vs-device-color failure: broken=%d fixed=%d",
                    sample.brokenError(),
                    sample.fixedError()));
        }

        System.out.printf(
                "index=%d original=%s paletteColor=0x%04X ff1(actual)=%s ff1(argb-misread)=%s fixedError=%d brokenError=%d%n",
                sample.index(),
                formatRgb(sample.originalRgb()),
                sample.paletteColor() & 0xFFFF,
                formatRgb(sample.actualRgb()),
                formatRgb(sample.brokenRgb()),
                sample.fixedError(),
                sample.brokenError());
    }

    private static byte[] readEntry() throws IOException {
        try (ZipFile zip = new ZipFile(GAME_JAR.toFile())) {
            ZipEntry entry = zip.getEntry(ENTRY_NAME);
            if (entry == null) {
                throw new IOException("Missing jar entry: " + ENTRY_NAME);
            }
            return zip.getInputStream(entry).readAllBytes();
        }
    }

    private static Sample pickBestSample(Raster raster, int width, int height, IndexColorModel colorModel, Palette palette) {
        int startY = java.lang.Math.max(0, height / 5);
        int endY = java.lang.Math.max(startY + 1, java.lang.Math.min(height, (height * 3) / 5));
        int[] counts = new int[256];
        for (int y = startY; y < endY; y++) {
            for (int x = 0; x < width; x++) {
                counts[raster.getSample(x, y, 0) & 0xFF]++;
            }
        }
        Sample best = null;
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == 0) {
                continue;
            }
            int originalArgb = colorModel.getRGB(i);
            int paletteColor = palette.getEntry(i);
            int[] originalRgb = rgb(originalArgb);
            int[] actualRgb = ff1Rgb(paletteColor);
            int[] brokenRgb = ff1Rgb(originalArgb);
            int fixedError = manhattan(originalRgb, actualRgb);
            int brokenError = manhattan(originalRgb, brokenRgb);
            Sample candidate = new Sample(i, paletteColor, originalRgb, actualRgb, brokenRgb, fixedError, brokenError);
            if (best == null) {
                best = candidate;
                continue;
            }
            int candidateImprovement = candidate.brokenError() - candidate.fixedError();
            int bestImprovement = best.brokenError() - best.fixedError();
            if (candidateImprovement > bestImprovement
                    || (candidateImprovement == bestImprovement && counts[i] > counts[best.index()])) {
                best = candidate;
            }
        }
        if (best == null) {
            throw new IllegalStateException("No indexed pixels found in FF1 logo band");
        }
        return best;
    }

    private static int[] ff1Rgb(int color) {
        return new int[]{
                (color & 0xF800) >>> 8,
                (color & 0x07E0) >>> 3,
                (color & 0x001F) << 3
        };
    }

    private static int[] rgb(int argb) {
        return new int[]{
                (argb >>> 16) & 0xFF,
                (argb >>> 8) & 0xFF,
                argb & 0xFF
        };
    }

    private static int manhattan(int[] expected, int[] actual) {
        return java.lang.Math.abs(expected[0] - actual[0])
                + java.lang.Math.abs(expected[1] - actual[1])
                + java.lang.Math.abs(expected[2] - actual[2]);
    }

    private static String formatRgb(int[] rgb) {
        return String.format("(%d,%d,%d)", rgb[0], rgb[1], rgb[2]);
    }

    private record Sample(
            int index,
            int paletteColor,
            int[] originalRgb,
            int[] actualRgb,
            int[] brokenRgb,
            int fixedError,
            int brokenError
    ) {
    }
}
