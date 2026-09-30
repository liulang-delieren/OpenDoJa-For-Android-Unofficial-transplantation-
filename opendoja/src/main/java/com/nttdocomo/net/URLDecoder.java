package com.nttdocomo.net;

import opendoja.host.DoJaEncoding;

/**
 * Decodes strings in {@code x-www-form-urlencoded} format to normal strings.
 */
public final class URLDecoder {
    /**
     * Creates a {@code URLDecoder}.
     */
    public URLDecoder() {
    }

    /**
     * Creates a string obtained by decoding a URL-encoded string.
     * After decoding {@code x-www-form-urlencoded} data, the decoded bytes are
     * converted to a Unicode string using the default encoding.
     *
     * @param str the string in URL-encoded form
     * @return the decoded string
     * @throws NullPointerException if {@code str} is {@code null}
     * @throws IllegalArgumentException if {@code str} is not in {@code x-www-form-urlencoded} format
     */
    public static String decode(String str) {
        if (str == null) {
            throw new NullPointerException("str");
        }
        return java.net.URLDecoder.decode(str, DoJaEncoding.DEFAULT_CHARSET);
    }
}
