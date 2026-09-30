package opendoja.host;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * OpenDoJa-owned implementation of the DoJa SDK's {@code SJIS_i} charset.
 *
 * <p>The mapping is mostly compatible with {@code windows-31j}, so this class keeps the
 * implementation small by delegating the shared middle to that charset and explicitly handling the
 * byte ranges and code points where the historical DoJa tables differ.</p>
 */
final class DoJaSjisCharset extends Charset {
    private static final Charset BASE = resolveBase();

    private static Charset resolveBase() {
        for (String candidate : new String[]{"windows-31j", "MS932", "Shift_JIS"}) {
            try {
                return Charset.forName(candidate);
            } catch (RuntimeException ignored) {
            }
        }
        throw new IllegalStateException("no SJIS base charset available");
    }

    private static final String HALF_WIDTH_KANA = "｡｢｣､･ｦｧｨｩｪｫｬｭｮｯｰ"
            + "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝﾞﾟ";
    // These byte-pair ranges decode under desktop windows-31j, but the bundled DoJa SJIS_i
    // tables do not define them at all. They must stay unmappable or the fallback path would
    // silently accept desktop-only vendor extensions.
    private static final int[] FORBIDDEN_PAIR_RANGE_STARTS = {
            0xED40, 0xED80, 0xEE40, 0xEE80, 0xEEEF,
            0xFA40, 0xFA80, 0xFB40, 0xFB80, 0xFC40
    };
    private static final int[] FORBIDDEN_PAIR_RANGE_ENDS = {
            0xED7E, 0xEDFC, 0xEE7E, 0xEEEC, 0xEEFC,
            0xFA7E, 0xFAFC, 0xFB7E, 0xFBFC, 0xFC4B
    };
    // These pairs need explicit encode handling or rejection because the host encoder would either
    // emit the wrong Unicode variant for DoJa or expose a mapping that SJIS_i does not permit.
    private static final int[] FORBIDDEN_ENCODE_PAIRS = {
            0x8143, 0x8145, 0x8150, 0x8160, 0x8161, 0x817C,
            0x8191, 0x8192, 0x81CA, 0x81E1, 0x81E2, 0x8394
    };
    // Shared bytes whose Unicode mapping differs between DoJa SJIS_i and desktop windows-31j.
    private static final int[] SPECIAL_DECODE_PAIRS = {
            0x8160, 0x8161, 0x817C, 0x8191, 0x8192, 0x81CA
    };
    private static final char[] SPECIAL_DECODE_CHARS = {
            '\u301C', '\u2016', '\u2212', '\u00A2', '\u00A3', '\u00AC'
    };
    private static final int[] SPECIAL_ENCODE_CODE_POINTS = {
            0x00A2, 0x00A3, 0x00AC, 0x2016, 0x2212, 0x301C
    };
    private static final int[] SPECIAL_ENCODE_PAIRS = {
            0x8191, 0x8192, 0x81CA, 0x8161, 0x817C, 0x8160
    };

    DoJaSjisCharset() {
        super("SJIS_i", charsetAliases());
    }

    @Override
    public boolean contains(Charset charset) {
        return charset instanceof DoJaSjisCharset || BASE.contains(charset);
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }

    private static String[] charsetAliases() {
        return new String[]{
                "SJIS",
                "Shift_JIS",
                "shift-jis",
                "MS_Kanji",
                "x-SJIS",
                "csShiftJIS"
        };
    }

    private static boolean isLeadByte(int value) {
        return (value >= 0x81 && value <= 0x9F) || (value >= 0xE0 && value <= 0xFC);
    }

    private static boolean isForbiddenPair(int pair) {
        for (int i = 0; i < FORBIDDEN_PAIR_RANGE_STARTS.length; i++) {
            if (pair >= FORBIDDEN_PAIR_RANGE_STARTS[i] && pair <= FORBIDDEN_PAIR_RANGE_ENDS[i]) {
                return true;
            }
        }
        return false;
    }

    private static boolean isForbiddenEncodePair(int pair) {
        for (int forbiddenPair : FORBIDDEN_ENCODE_PAIRS) {
            if (pair == forbiddenPair) {
                return true;
            }
        }
        return false;
    }

    private static char decodeOverride(int pair) {
        for (int i = 0; i < SPECIAL_DECODE_PAIRS.length; i++) {
            if (pair == SPECIAL_DECODE_PAIRS[i]) {
                return SPECIAL_DECODE_CHARS[i];
            }
        }
        return 0;
    }

    private static int encodeOverride(int codePoint) {
        for (int i = 0; i < SPECIAL_ENCODE_CODE_POINTS.length; i++) {
            if (codePoint == SPECIAL_ENCODE_CODE_POINTS[i]) {
                return SPECIAL_ENCODE_PAIRS[i];
            }
        }
        return -1;
    }

    private static final class Decoder extends CharsetDecoder {
        private final CharsetDecoder base = BASE.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        private Decoder(Charset charset) {
            super(charset, 0.5f, 1.0f);
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            while (in.hasRemaining()) {
                if (!out.hasRemaining()) {
                    return CoderResult.OVERFLOW;
                }
                int position = in.position();
                int first = in.get(position) & 0xFF;
                if (first <= 0x7F) {
                    out.put((char) first);
                    in.position(position + 1);
                    continue;
                }
                if (first >= 0xA1 && first <= 0xDF) {
                    out.put(HALF_WIDTH_KANA.charAt(first - 0xA1));
                    in.position(position + 1);
                    continue;
                }
                if (!isLeadByte(first)) {
                    return CoderResult.malformedForLength(1);
                }
                if (in.remaining() < 2) {
                    return CoderResult.UNDERFLOW;
                }
                int second = in.get(position + 1) & 0xFF;
                int pair = (first << 8) | second;
                char override = decodeOverride(pair);
                if (override != 0) {
                    out.put(override);
                    in.position(position + 2);
                    continue;
                }
                // Reject windows-31j-only extension ranges before we fall through to the shared
                // host decoder.
                if (isForbiddenPair(pair)) {
                    return CoderResult.unmappableForLength(2);
                }
                CoderResult result = decodeBasePair(pair, out);
                if (result != null) {
                    return result;
                }
                in.position(position + 2);
            }
            return CoderResult.UNDERFLOW;
        }

        private CoderResult decodeBasePair(int pair, CharBuffer out) {
            base.reset();
            CharBuffer decoded = CharBuffer.allocate(2);
            CoderResult result = base.decode(ByteBuffer.wrap(new byte[]{
                    (byte) (pair >> 8),
                    (byte) pair
            }), decoded, true);
            if (result.isError()) {
                return result;
            }
            result = base.flush(decoded);
            if (result.isError()) {
                return result;
            }
            decoded.flip();
            while (decoded.hasRemaining()) {
                if (!out.hasRemaining()) {
                    return CoderResult.OVERFLOW;
                }
                out.put(decoded.get());
            }
            return null;
        }
    }

    private static final class Encoder extends CharsetEncoder {
        private final CharsetEncoder base = BASE.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        private Encoder(Charset charset) {
            super(charset, 1.0f, 2.0f);
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            while (in.hasRemaining()) {
                int position = in.position();
                char first = in.get(position);
                if (first <= 0x7F) {
                    if (out.remaining() < 1) {
                        return CoderResult.OVERFLOW;
                    }
                    out.put((byte) first);
                    in.position(position + 1);
                    continue;
                }
                int kanaIndex = HALF_WIDTH_KANA.indexOf(first);
                if (kanaIndex >= 0) {
                    if (out.remaining() < 1) {
                        return CoderResult.OVERFLOW;
                    }
                    out.put((byte) (0xA1 + kanaIndex));
                    in.position(position + 1);
                    continue;
                }
                int overridePair = encodeOverride(first);
                if (overridePair >= 0) {
                    if (out.remaining() < 2) {
                        return CoderResult.OVERFLOW;
                    }
                    putPair(out, overridePair);
                    in.position(position + 1);
                    continue;
                }

                int inputLength = 1;
                char[] chars = {first};
                if (Character.isHighSurrogate(first)) {
                    if (in.remaining() < 2) {
                        return CoderResult.UNDERFLOW;
                    }
                    char second = in.get(position + 1);
                    if (!Character.isLowSurrogate(second)) {
                        return CoderResult.malformedForLength(1);
                    }
                    chars = new char[]{first, second};
                    inputLength = 2;
                } else if (Character.isLowSurrogate(first)) {
                    return CoderResult.malformedForLength(1);
                }

                byte[] encoded;
                try {
                    encoded = encodeWithBase(chars);
                } catch (CharacterCodingException exception) {
                    return CoderResult.unmappableForLength(inputLength);
                }
                // The host encoder knows about windows-31j, not DoJa SJIS_i. Filter any bytes
                // that belong to desktop-only extensions or to code points we already override.
                if (isForbiddenEncodedBytes(encoded)) {
                    return CoderResult.unmappableForLength(inputLength);
                }
                if (out.remaining() < encoded.length) {
                    return CoderResult.OVERFLOW;
                }
                out.put(encoded);
                in.position(position + inputLength);
            }
            return CoderResult.UNDERFLOW;
        }

        private byte[] encodeWithBase(char[] chars) throws CharacterCodingException {
            base.reset();
            ByteBuffer encoded = base.encode(CharBuffer.wrap(chars));
            byte[] bytes = new byte[encoded.remaining()];
            encoded.get(bytes);
            return bytes;
        }

        private boolean isForbiddenEncodedBytes(byte[] encoded) {
            if (encoded.length == 1) {
                int value = encoded[0] & 0xFF;
                // Non-ASCII use of these byte slots is a windows-31j detail; DoJa keeps them for
                // plain ASCII backslash and tilde only.
                return value == 0x5C || value == 0x7E;
            }
            if (encoded.length != 2) {
                return false;
            }
            int pair = ((encoded[0] & 0xFF) << 8) | (encoded[1] & 0xFF);
            return isForbiddenPair(pair) || isForbiddenEncodePair(pair);
        }

        private void putPair(ByteBuffer out, int pair) {
            out.put((byte) (pair >> 8));
            out.put((byte) pair);
        }
    }
}
