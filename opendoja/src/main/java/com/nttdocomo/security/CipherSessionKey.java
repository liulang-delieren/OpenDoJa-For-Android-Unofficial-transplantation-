package com.nttdocomo.security;

import com.nttdocomo.lang.IllegalStateException;
import opendoja.host.security.DoJaCryptoSupport;

/**
 * Defines a session-key object.
 */
public final class CipherSessionKey {
    private final int sessionCipher;
    private final byte[] sessionKey;
    private final byte[] encryptedSessionKey;

    CipherSessionKey(CipherPBE cipherPBE, int cipher) throws CipherException {
        DoJaCryptoSupport.AlgorithmSpec sessionSpec = DoJaCryptoSupport.algorithmSpec(cipher);
        byte[] rawSessionKey = DoJaCryptoSupport.randomBytes(sessionSpec.keyLengthBytes());
        byte[] wrapped = DoJaCryptoSupport.wrapSessionKey(rawSessionKey, cipher);
        this.sessionCipher = cipher;
        this.sessionKey = rawSessionKey;
        this.encryptedSessionKey = cipherPBE.encryptWrappedSessionKey(wrapped);
    }

    CipherSessionKey(CipherPBE cipherPBE, byte[] encryptedSessionKey) throws CipherException {
        byte[] wrapped = cipherPBE.decryptWrappedSessionKey(encryptedSessionKey);
        DoJaCryptoSupport.WrappedSessionKey unwrapped = DoJaCryptoSupport.unwrapSessionKey(wrapped);
        this.sessionCipher = unwrapped.sessionCipher();
        this.sessionKey = unwrapped.sessionKey();
        this.encryptedSessionKey = encryptedSessionKey.clone();
    }

    /**
     * Gets the encrypted session key.
     * The key value returned is a copy of the array held internally by this
     * object.
     *
     * @return the encrypted session key as a byte array
     */
    public byte[] getEncryptedSessionKey() {
        return encryptedSessionKey.clone();
    }

    /**
     * Creates a symmetric-cipher object from the session key, the specified
     * encryption method, and the initialization vector.
     *
     * @param cipher the encryption method defined by {@link SymmetricCipher}
     * @param iv the initialization vector
     * @param encrypt {@code true} for encryption, or {@code false} for
     *        decryption
     * @return the {@link SymmetricCipher} object
     * @throws NullPointerException if {@code iv} is {@code null}
     * @throws IllegalArgumentException if the parameters are inconsistent with
     *         the session key
     * @throws IllegalStateException if the {@link SymmetricCipher} object
     *         cannot be created
     */
    public synchronized SymmetricCipher createSymmetricCipher(int cipher, byte[] iv, boolean encrypt) {
        try {
            return new SymmetricCipher(cipher, this, iv, encrypt);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    int sessionCipher() {
        return sessionCipher;
    }

    byte[] sessionKey() {
        return sessionKey.clone();
    }
}
