package com.nttdocomo.security;

import opendoja.host.security.DoJaCryptoSupport;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

/**
 * Defines a symmetric-key cipher object.
 */
public final class SymmetricCipher {
    /** Represents DES with CBC mode and PKCS#7 padding (=1). */
    public static final int DES_CBC_PKCS7PADDING = 1;

    /** Represents triple DES with CBC mode and PKCS#7 padding (=2). */
    public static final int DES_EDE_CBC_PKCS7PADDING = 2;

    /** Represents AES (128-bit key) with CBC mode and PKCS#7 padding (=3). */
    public static final int AES_128_CBC_PKCS7PADDING = 3;

    /** Represents AES (192-bit key) with CBC mode and PKCS#7 padding (=4). */
    public static final int AES_192_CBC_PKCS7PADDING = 4;

    /** Represents AES (256-bit key) with CBC mode and PKCS#7 padding (=5). */
    public static final int AES_256_CBC_PKCS7PADDING = 5;

    private final DoJaCryptoSupport.AlgorithmSpec spec;
    private final byte[] sessionKey;
    private final byte[] iv;
    private final boolean encrypt;
    private javax.crypto.Cipher cipher;
    private boolean lastCallWasExecute;

    SymmetricCipher(int cipher, CipherSessionKey sessionKey, byte[] iv, boolean encrypt) {
        if (iv == null) {
            throw new NullPointerException("iv");
        }
        this.spec = DoJaCryptoSupport.algorithmSpec(cipher);
        if (sessionKey.sessionCipher() != cipher) {
            throw new IllegalArgumentException("Session-key cipher does not match: " + cipher);
        }
        this.sessionKey = sessionKey.sessionKey();
        if (this.sessionKey.length != spec.keyLengthBytes()) {
            throw new IllegalArgumentException("Session-key length does not match the cipher");
        }
        if (iv.length != spec.blockSizeBytes()) {
            throw new IllegalArgumentException("iv size does not match the cipher block size");
        }
        this.iv = iv.clone();
        this.encrypt = encrypt;
        reset();
    }

    /**
     * Encrypts or decrypts the specified data.
     * If the total size of the excess data held by this object and the
     * specified data does not reach the block size, or if {@code data} is
     * {@code null}, {@code null} is returned.
     *
     * @param data the data to encrypt or decrypt
     * @return the encrypted or decrypted data
     */
    public synchronized byte[] execute(byte[] data) {
        if (data == null || data.length == 0) {
            lastCallWasExecute = false;
            return null;
        }
        byte[] result = cipher.update(data.clone());
        lastCallWasExecute = true;
        return result == null || result.length == 0 ? null : result.clone();
    }

    /**
     * Encrypts or decrypts the specified data, including the padding
     * processing for the final block.
     *
     * @param data the data to encrypt or decrypt
     * @return the encrypted or decrypted data
     * @throws CipherException if padding cannot be removed during decryption
     */
    public synchronized byte[] executeFinal(byte[] data) throws CipherException {
        if (data == null && !lastCallWasExecute) {
            return null;
        }
        try {
            byte[] result = cipher.doFinal(data == null ? new byte[0] : data.clone());
            reset();
            return result == null || result.length == 0 ? null : result.clone();
        } catch (GeneralSecurityException exception) {
            reset();
            throw DoJaCryptoSupport.translateCipherFinalException(exception);
        }
    }

    /**
     * Restores the state of the object to the state at object creation time.
     * Use this method if encryption or decryption is aborted after
     * {@link #execute(byte[])} and before {@link #executeFinal(byte[])} and a
     * new encryption or decryption operation is to be started again.
     */
    public synchronized void reset() {
        try {
            SecretKey secretKey = new SecretKeySpec(sessionKey, spec.keyAlgorithm());
            this.cipher = DoJaCryptoSupport.newCipher(spec, secretKey, iv, encrypt);
            this.lastCallWasExecute = false;
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
