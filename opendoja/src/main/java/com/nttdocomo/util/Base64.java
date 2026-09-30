package com.nttdocomo.util;

import opendoja.host.DoJaEncoding;

/**
 * Performs Base64 encoding and decoding.
 * See RFC 2045 for the Base64 encoding format.
 */
public final class Base64 {
    private Base64() {
    }

    /**
     * Decodes a Base64 string.
     *
     * @param value the string to decode
     * @return the decoded byte array
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is not valid Base64
     */
    public static byte[] decode(String value) {
        return java.util.Base64.getMimeDecoder().decode(value);
    }

    /**
     * Decodes a Base64 byte sequence.
     *
     * @param value the byte sequence to decode
     * @return the decoded byte array
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is not valid Base64
     */
    public static byte[] decode(byte[] value) {
        return java.util.Base64.getMimeDecoder().decode(value);
    }

    /**
     * Decodes part of a Base64 byte sequence.
     *
     * @param value the byte sequence to decode
     * @param offset the offset of the range to decode
     * @param length the length of the range to decode
     * @return the decoded byte array
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws ArrayIndexOutOfBoundsException if the specified range is invalid
     * @throws IllegalArgumentException if the specified range is not valid Base64
     */
    public static byte[] decode(byte[] value, int offset, int length) {
        byte[] slice = new byte[length];
        System.arraycopy(value, offset, slice, 0, length);
        return decode(slice);
    }

    /**
     * Encodes a string as Base64.
     * The Unicode string is first converted with the platform default DoJa encoding.
     *
     * @param value the string to encode
     * @return the Base64-encoded string
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static String encode(String value) {
        return encode(value.getBytes(DoJaEncoding.DEFAULT_CHARSET));
    }

    /**
     * Encodes a byte sequence as Base64.
     *
     * @param value the byte sequence to encode
     * @return the Base64-encoded string
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static String encode(byte[] value) {
        return java.util.Base64.getEncoder().encodeToString(value);
    }

    /**
     * Encodes part of a byte sequence as Base64.
     *
     * @param value the byte sequence to encode
     * @param offset the offset of the range to encode
     * @param length the length of the range to encode
     * @return the Base64-encoded string
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws ArrayIndexOutOfBoundsException if the specified range is invalid
     */
    public static String encode(byte[] value, int offset, int length) {
        byte[] slice = new byte[length];
        System.arraycopy(value, offset, slice, 0, length);
        return encode(slice);
    }
}
