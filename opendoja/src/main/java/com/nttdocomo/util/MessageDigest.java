package com.nttdocomo.util;

import java.security.NoSuchAlgorithmException;

/**
 * Class for calculating message digests.
 * It calculates a hash value from arbitrary byte data.
 * The output length is a fixed length that depends on the message-digest
 * algorithm.
 *
 * <p>To obtain a {@code MessageDigest} object for calculating a message
 * digest, specify the algorithm name in {@link #getInstance(String)}.
 * Input data is added with the {@code update} methods, and the calculation is
 * finalized with the {@code digest} methods. Calling {@link #reset()} resets
 * the digest to its initial state. After a {@code digest} call, the object
 * also returns to the same initial state.</p>
 *
 * <p>Minimum specification: the minimum specification supports the MD5
 * (RFC 1321) and SHA-1 (NIST FIPS 180-1) message-digest algorithms.</p>
 *
 * <p>Introduced in DoJa-3.0 (505i).</p>
 */
public class MessageDigest {
    private final java.security.MessageDigest delegate;

    private MessageDigest(java.security.MessageDigest delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a {@code MessageDigest} object for the specified algorithm.
     *
     * @param algorithm the message-digest algorithm
     * @return the {@code MessageDigest} object for the specified algorithm
     * @throws NullPointerException if {@code algorithm} is {@code null}
     * @throws IllegalArgumentException if the specified algorithm is not
     *         supported
     */
    public static MessageDigest getInstance(String algorithm) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm");
        }
        try {
            return new MessageDigest(java.security.MessageDigest.getInstance(algorithm));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported digest algorithm: " + algorithm, e);
        }
    }

    /**
     * Updates the digest using the specified byte data as input.
     *
     * @param input the input byte data
     */
    public void update(byte input) {
        delegate.update(input);
    }

    /**
     * Updates the digest using part of the specified byte array as input.
     *
     * @param input the input byte array
     * @param offset the start position in the byte array
     * @param length the length in the byte array
     * @throws NullPointerException if {@code input} is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code offset} is negative,
     *         {@code length} is negative, or {@code offset + length} exceeds
     *         the length of {@code input}
     */
    public void update(byte[] input, int offset, int length) {
        delegate.update(input, offset, length);
    }

    /**
     * Updates the digest using the specified byte array as input.
     *
     * @param input the input byte array
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public void update(byte[] input) {
        delegate.update(input);
    }

    /**
     * Performs the final processing, such as padding, and obtains the digest.
     * After this call, the digest is reset.
     *
     * @return the digest value as a byte array
     */
    public byte[] digest() {
        return delegate.digest();
    }

    /**
     * Performs the final processing, such as padding, and obtains the digest.
     * The digest value is stored in the specified array.
     * After this call, the digest is reset.
     * If {@code length} is larger than the digest length, the part of the array
     * beyond the digest length is left unchanged.
     *
     * @param buffer the byte array into which the digest value is written
     * @param offset the start position in the byte array
     * @param length the length in the byte array
     * @return the number of bytes stored in {@code buffer}
     * @throws NullPointerException if {@code buffer} is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code offset} is negative,
     *         {@code length} is negative, {@code offset + length} exceeds the
     *         length of {@code buffer}, or {@code length} is smaller than the
     *         digest length
     */
    public int digest(byte[] buffer, int offset, int length) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        if (offset < 0 || length < 0 || offset + length > buffer.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        byte[] result = delegate.digest();
        if (length < result.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int copyLength = result.length;
        System.arraycopy(result, 0, buffer, offset, copyLength);
        return copyLength;
    }

    /**
     * Updates the digest using the specified byte array as input, then obtains
     * the digest.
     * This is equivalent to performing {@code update(input)} and then
     * {@code digest()}.
     * After this call, the digest is reset.
     *
     * @param input the input byte array
     * @return the digest value as a byte array
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public byte[] digest(byte[] input) {
        return delegate.digest(input);
    }

    /**
     * Resets the digest.
     */
    public void reset() {
        delegate.reset();
    }

    /**
     * Gets the string representation of this digest.
     * A string representation such as
     * {@code "MD5 Message Digest, <initialized>"} is returned, but the format
     * is device-dependent and is not specified.
     *
     * @return the string representation of this digest
     */
    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * Gets the digest length in bytes.
     *
     * @return the digest length in bytes
     */
    public final int getDigestLength() {
        return delegate.getDigestLength();
    }

    /**
     * Gets the algorithm name of this digest.
     *
     * @return the digest algorithm name
     */
    public final String getAlgorithm() {
        return delegate.getAlgorithm();
    }

    /**
     * Compares two digests for equality.
     *
     * @param left one digest to compare
     * @param right the other digest to compare
     * @return {@code true} if the digests are equal; {@code false} otherwise
     * @throws NullPointerException if either or both arguments are
     *         {@code null}
     */
    public static boolean isEqual(byte[] left, byte[] right) {
        if (left == null || right == null) {
            throw new NullPointerException(left == null ? "left" : "right");
        }
        return java.security.MessageDigest.isEqual(left, right);
    }
}
