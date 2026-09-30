package com.nttdocomo.security;

import com.nttdocomo.lang.IllegalStateException;
import com.nttdocomo.lang.UnsupportedOperationException;
import opendoja.host.security.DoJaCryptoSupport;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

/**
 * Defines a password-based-encryption object.
 */
public final class CipherPBE {
    /** Indicates that the PBE-key generation algorithm is SHA1 (=1). */
    public static final int HASH_SHA1 = 1;

    private final byte[] iv;
    private final DoJaCryptoSupport.AlgorithmSpec wrappingSpec;
    private final SecretKey pbeKey;

    /**
     * Creates a PBE object.
     * When the PBE key is created, the specified password, salt, iteration
     * count, hash algorithm, and encryption method are used.
     * When a session key is encrypted or decrypted with the PBE key, the
     * specified encryption method and initialization vector are used.
     * Passwords are supported at minimum for ASCII graphic characters
     * (0x20-0x7e).
     *
     * @param password the password
     * @param salt the salt
     * @param iterationCount the iteration count
     * @param hashAlgorithm the hash algorithm
     * @param cipher the encryption method defined by {@link SymmetricCipher}
     * @param iv the initialization vector
     * @throws UnsupportedOperationException if the terminal does not support the
     *         encryption function
     * @throws NullPointerException if {@code password}, {@code salt}, or
     *         {@code iv} is {@code null}
     * @throws IllegalArgumentException if a parameter value is invalid
     */
    public CipherPBE(byte[] password, byte[] salt, int iterationCount, int hashAlgorithm, int cipher, byte[] iv) {
        if (password == null) {
            throw new NullPointerException("password");
        }
        if (salt == null) {
            throw new NullPointerException("salt");
        }
        if (iv == null) {
            throw new NullPointerException("iv");
        }
        if (password.length < 1) {
            throw new IllegalArgumentException("password length must be at least 1");
        }
        if (salt.length < 8) {
            throw new IllegalArgumentException("salt length must be at least 8");
        }
        if (iterationCount < 1000) {
            throw new IllegalArgumentException("iterationCount must be at least 1000");
        }
        if (hashAlgorithm != HASH_SHA1) {
            throw new IllegalArgumentException("Unsupported hash algorithm: " + hashAlgorithm);
        }
        try {
            this.wrappingSpec = DoJaCryptoSupport.algorithmSpec(cipher);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
        if (iv.length != wrappingSpec.blockSizeBytes()) {
            throw new IllegalArgumentException("iv size does not match the cipher block size");
        }
        try {
            this.pbeKey = DoJaCryptoSupport.derivePbeKey(password, salt, iterationCount, wrappingSpec);
            DoJaCryptoSupport.newCipher(wrappingSpec, pbeKey, iv, true);
        } catch (GeneralSecurityException exception) {
            throw new UnsupportedOperationException(exception.getMessage());
        }
        this.iv = iv.clone();
    }

    /**
     * Creates a {@link CipherSessionKey} object.
     * Each time this method is called, a session key is created and a
     * {@link CipherSessionKey} object is generated.
     *
     * @param cipher the encryption method defined by {@link SymmetricCipher}
     * @return the {@link CipherSessionKey} object
     * @throws IllegalArgumentException if {@code cipher} is invalid
     * @throws IllegalStateException if the {@link CipherSessionKey} object
     *         cannot be created
     */
    public synchronized CipherSessionKey createCipherSessionKey(int cipher) {
        try {
            return new CipherSessionKey(this, cipher);
        } catch (CipherException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    /**
     * Creates a {@link CipherSessionKey} object from the specified encrypted
     * session key.
     *
     * @param encryptedSessionKey the encrypted session key
     * @return the {@link CipherSessionKey} object
     * @throws NullPointerException if {@code encryptedSessionKey} is
     *         {@code null}
     * @throws CipherException if the encrypted session key is invalid
     * @throws IllegalStateException if the {@link CipherSessionKey} object
     *         cannot be created
     */
    public synchronized CipherSessionKey createCipherSessionKey(byte[] encryptedSessionKey) throws CipherException {
        if (encryptedSessionKey == null) {
            throw new NullPointerException("encryptedSessionKey");
        }
        try {
            return new CipherSessionKey(this, encryptedSessionKey);
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    byte[] encryptWrappedSessionKey(byte[] wrappedSessionKey) throws CipherException {
        try {
            return DoJaCryptoSupport.newCipher(wrappingSpec, pbeKey, iv, true).doFinal(wrappedSessionKey);
        } catch (GeneralSecurityException exception) {
            throw new CipherException(CipherException.UNDEFINED, exception.getMessage());
        }
    }

    byte[] decryptWrappedSessionKey(byte[] encryptedSessionKey) throws CipherException {
        try {
            return DoJaCryptoSupport.newCipher(wrappingSpec, pbeKey, iv, false).doFinal(encryptedSessionKey);
        } catch (GeneralSecurityException exception) {
            throw DoJaCryptoSupport.translateCipherFinalException(exception);
        }
    }
}
