package opendoja.probes;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

/**
 * Verifies that the compatibility hook redirects DoJa-era SJIS lookups to the OpenDoJa-owned
 * {@code SJIS_i} mapping, including the {@code 0xF994 -> U+E6EF} emoji path reported by POKER F.
 */
public final class SjisEmojiProbe {
    private static final byte[] TWO_HEARTS_SJIS = {(byte) 0xF9, (byte) 0x94};
    private static final byte[] WAVE_DASH_SJIS = {(byte) 0x81, (byte) 0x60};
    private static final byte[] INVALID_HOST_EXTENSION_SJIS = {(byte) 0xED, (byte) 0x40};

    private SjisEmojiProbe() {
    }

    public static void main(String[] args) throws Exception {
        assertCodePoints("SJIS before install", "SJIS", "U+FFFD", "U+FFFD");
        assertCodePoints("Shift_JIS before install", "Shift_JIS", "U+FFFD", "U+FFFD");
        probeNamedCharset("MS932");
        probeNamedCharset("windows-31j");
        installDoJaSjisCompatibility();
        assertCodePoints("SJIS after install", "SJIS", "U+E6EF");
        assertCodePoints("Shift_JIS after install", "Shift_JIS", "U+E6EF");
        assertBytesDecode("SJIS wave dash after install", WAVE_DASH_SJIS, "SJIS", "U+301C");
        assertBytesDecode("SJIS invalid host extension after install", INVALID_HOST_EXTENSION_SJIS, "SJIS", "U+FFFD");
        assertEncodedBytes("SJIS wave dash encode", "SJIS", "\u301C", "8160");
        assertEncodedBytes("SJIS two hearts encode", "SJIS", "\uE6EF", "F994");
        assertUnmappable("SJIS fullwidth tilde encode", "SJIS", "\uFF5E");
        System.out.println("SJIS emoji probe OK");
    }

    private static void probeNamedCharset(String name) throws Exception {
        Charset charset = Charset.forName(name);
        String decoded = new String(TWO_HEARTS_SJIS, name);
        System.out.println(name
                + " canonical=" + charset.name()
                + " codePoints=" + decoded.codePoints()
                .mapToObj(codePoint -> String.format("U+%04X", codePoint))
                .toList());
    }

    private static void installDoJaSjisCompatibility() throws Exception {
        Class<?> compatibility = Class.forName("opendoja.host.DoJaSjisCompatibility");
        Method install = compatibility.getDeclaredMethod("install");
        install.setAccessible(true);
        install.invoke(null);
    }

    private static void assertBytesDecode(String label, byte[] bytes, String charsetName, String... expected) throws Exception {
        String decoded = new String(bytes, charsetName);
        String[] actual = decoded.codePoints()
                .mapToObj(codePoint -> String.format("U+%04X", codePoint))
                .toArray(String[]::new);
        if (!java.util.Arrays.equals(expected, actual)) {
            throw new AssertionError(label + " expected " + java.util.Arrays.toString(expected)
                    + " but was " + java.util.Arrays.toString(actual));
        }
    }

    private static void assertEncodedBytes(String label, String charsetName, String text, String expectedHex) throws Exception {
        byte[] encoded = text.getBytes(charsetName);
        StringBuilder actual = new StringBuilder();
        for (byte value : encoded) {
            actual.append(String.format("%02X", value & 0xFF));
        }
        if (!expectedHex.equals(actual.toString())) {
            throw new AssertionError(label + " expected " + expectedHex + " but was " + actual);
        }
    }

    private static void assertUnmappable(String label, String charsetName, String text) throws Exception {
        try {
            Charset.forName(charsetName).newEncoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                    .encode(java.nio.CharBuffer.wrap(text));
            throw new AssertionError(label + " expected an unmappable-character failure");
        } catch (java.nio.charset.CharacterCodingException expected) {
            return;
        }
    }

    private static void assertCodePoints(String label, String charsetName, String... expected) throws Exception {
        String decoded = new String(TWO_HEARTS_SJIS, charsetName);
        String[] actual = decoded.codePoints()
                .mapToObj(codePoint -> String.format("U+%04X", codePoint))
                .toArray(String[]::new);
        if (!java.util.Arrays.equals(expected, actual)) {
            throw new AssertionError(label + " expected " + java.util.Arrays.toString(expected)
                    + " but was " + java.util.Arrays.toString(actual));
        }
        probeNamedCharset(charsetName);
    }
}
