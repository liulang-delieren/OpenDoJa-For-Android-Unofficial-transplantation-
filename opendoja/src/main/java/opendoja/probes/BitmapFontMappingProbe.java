package opendoja.probes;

import com.nttdocomo.ui.Font;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Proves the bundled bitmap font already contains the DoJa emoji glyph, so the POKER F bug lives
 * in charset decoding rather than in glyph data.
 */
public final class BitmapFontMappingProbe {
    private static final int DOJA_SJIS_HEARTS = 0xE6EF;
    private static final int QUESTION_MARK = 0x003F;

    private BitmapFontMappingProbe() {
    }

    public static void main(String[] args) throws Exception {
        verifyGlyphMapping(Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, 12), "12px");
        verifyGlyphMapping(Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, 16), "16px");
        System.out.println("Bitmap font mapping probe OK");
    }

    private static void verifyGlyphMapping(Font font, String label) throws Exception {
        Method glyphIndexFor = font.getClass().getDeclaredMethod("glyphIndexFor", int.class);
        glyphIndexFor.setAccessible(true);
        int emojiIndex = (Integer) glyphIndexFor.invoke(font, DOJA_SJIS_HEARTS);
        int questionIndex = (Integer) glyphIndexFor.invoke(font, QUESTION_MARK);
        check(emojiIndex >= 0, label + " DoJa SJIS emoji should resolve to a glyph index");
        check(emojiIndex != questionIndex, label + " DoJa SJIS emoji should not map to the question-mark glyph");

        Field strikeField = font.getClass().getDeclaredField("strike");
        strikeField.setAccessible(true);
        Object strike = strikeField.get(font);
        Method bytesPerGlyph = strike.getClass().getDeclaredMethod("bytesPerGlyph");
        bytesPerGlyph.setAccessible(true);
        Method glyphData = strike.getClass().getDeclaredMethod("glyphData");
        glyphData.setAccessible(true);
        Method codePointToGlyph = strike.getClass().getDeclaredMethod("codePointToGlyph");
        codePointToGlyph.setAccessible(true);
        byte[] glyphBytes = (byte[]) glyphData.invoke(strike);
        int glyphSize = (Integer) bytesPerGlyph.invoke(strike);
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> mapping = (Map<Integer, Integer>) codePointToGlyph.invoke(strike);
        // Check both the logical mapping and the underlying glyph bytes so a future fallback path
        // cannot accidentally pass by reusing the question-mark slot.
        check(mapping.get(DOJA_SJIS_HEARTS) == emojiIndex, label + " mapping table should point at the resolved emoji index");
        check(mapping.get(QUESTION_MARK) == questionIndex, label + " mapping table should point at the resolved question-mark index");
        check(anyNonZero(glyphBytes, emojiIndex * glyphSize, glyphSize), label + " emoji glyph bytes should not be empty");
        check(anyNonZero(glyphBytes, questionIndex * glyphSize, glyphSize), label + " question-mark glyph bytes should not be empty");
    }

    private static boolean anyNonZero(byte[] bytes, int offset, int length) {
        for (int index = offset; index < offset + length; index++) {
            if (bytes[index] != 0) {
                return true;
            }
        }
        return false;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
