package opendoja.host;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Service-loaded charset provider that exposes OpenDoJa's {@code SJIS_i} mapping under the DoJa
 * era names titles commonly request.
 */
public final class DoJaSjisCharsetProvider extends CharsetProvider {
    private static final Charset SJIS_I = new DoJaSjisCharset();
    private static final Map<String, Charset> CHARSETS = createCharsets();

    static Charset sjisCharset() {
        return SJIS_I;
    }

    @Override
    public Iterator<Charset> charsets() {
        return java.util.List.of(SJIS_I).iterator();
    }

    @Override
    public Charset charsetForName(String charsetName) {
        if (charsetName == null) {
            return null;
        }
        return CHARSETS.get(charsetName.toLowerCase(Locale.ROOT));
    }

    private static Map<String, Charset> createCharsets() {
        LinkedHashMap<String, Charset> charsets = new LinkedHashMap<>();
        register(charsets, SJIS_I);
        return charsets;
    }

    private static void register(Map<String, Charset> charsets, Charset charset) {
        // Charset lookup is case-insensitive, but the provider API is not, so normalize once.
        charsets.put(charset.name().toLowerCase(Locale.ROOT), charset);
        for (String alias : charset.aliases()) {
            charsets.put(alias.toLowerCase(Locale.ROOT), charset);
        }
    }
}
