package opendoja.probes;

import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.IApplication;
import com.nttdocomo.ui.Image;
import opendoja.host.DesktopLauncher;
import opendoja.host.LaunchConfig;

import java.util.Arrays;

public final class BitmapFontStrikeProbe {
    private static final String LAST_8PX_GLYPH = "\uE70B";
    private static final String FIRST_MISSING_8PX_GLYPH = "\uE70C";
    private static final String LAST_10PX_GLYPH = "\uE757";

    private BitmapFontStrikeProbe() {
    }

    public static void main(String[] args) {
        Font font8 = Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, 8);
        Font font10 = Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, 10);
        Font question8 = Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, 8);
        Font question10 = Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, 10);

        assertArrayEquals("supportedSizes", new int[]{8, 10, 12, 16, 20, 24, 30}, Font.getSupportedFontSizes());
        assertEquals("font8Height", 8, font8.getHeight());
        assertEquals("font10Height", 10, font10.getHeight());
        assertEquals("font9FloorsTo8", 8, Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, 9).getHeight());
        assertEquals("font11FloorsTo10", 10, Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, 11).getHeight());

        int question8Hash = renderHash(question8, "?");
        int question10Hash = renderHash(question10, "?");
        assertNotEquals("font8LastPresentGlyph", question8Hash, renderHash(font8, LAST_8PX_GLYPH));
        assertEquals("font8FirstMissingGlyphFallsBack", question8Hash, renderHash(font8, FIRST_MISSING_8PX_GLYPH));
        assertNotEquals("font10TailGlyphPresent", question10Hash, renderHash(font10, LAST_10PX_GLYPH));
        IApplication legacyApp = DesktopLauncher.launch(LaunchConfig.builder(LegacySmallStrikeApp.class)
                .parameter("ProfileVer", "DoJa-1.0")
                .build());
        legacyApp.terminate();

        System.out.println("Bitmap font strike probe OK");
    }

    private static int renderHash(Font font, String text) {
        Image image = Image.createImage(64, 64);
        Graphics graphics = image.getGraphics();
        graphics.setFont(font);
        graphics.setColor(0xFFFFFFFF);
        graphics.drawString(text, 0, font.getAscent());
        int hash = 1;
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                hash = 31 * hash + graphics.getPixel(x, y);
            }
        }
        return hash;
    }

    private static void assertEquals(String label, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertNotEquals(String label, int unexpected, int actual) {
        if (unexpected == actual) {
            throw new AssertionError(label + " unexpectedly matched " + actual);
        }
    }

    private static void assertArrayEquals(String label, int[] expected, int[] actual) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(label + " expected " + Arrays.toString(expected)
                    + " but was " + Arrays.toString(actual));
        }
    }

    public static final class LegacySmallStrikeApp extends IApplication {
        public LegacySmallStrikeApp() {
            assertEquals("legacySmallHeight", 10,
                    Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, Font.SIZE_SMALL).getHeight());
        }

        @Override
        public void start() {
        }
    }
}
