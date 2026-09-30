package com.nttdocomo.security;

import com.nttdocomo.lang.IllegalStateException;
import com.nttdocomo.lang.UnsupportedOperationException;
import com.nttdocomo.system.StoreException;
import com.nttdocomo.ui.MApplication;
import com.nttdocomo.util.Phone;
import opendoja.host.DoJaRuntime;
import opendoja.host.security.DoJaCryptoSupport;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates PKCS#7 SignedData.
 */
public class PKCS7Signer {
    /** Indicates ordinary data content (=0). */
    public static final int DATA = 0;

    /** Indicates digital-signed-data content (=1). */
    public static final int SIGNED_DATA = 1;

    private static final AtomicBoolean SIGNING = new AtomicBoolean();

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private int contentType = DATA;
    private String digestAlgorithm = "SHA-1";

    /**
     * Creates a {@code PKCS7Signer} object.
     *
     * @throws IllegalStateException if UIM information could not be acquired
     * @throws UnsupportedOperationException if the inserted UIM version is not
     *         Version 2 or later
     */
    public PKCS7Signer() {
        ensureDigitalSignatureAvailable();
        try {
            DoJaCryptoSupport.hostIdentity();
        } catch (java.lang.IllegalStateException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    /**
     * Gets the content type set in this object.
     *
     * @return the content type
     */
    public int getContentType() {
        return contentType;
    }

    /**
     * Sets the content type of the data to be signed.
     * The default value of the content type is {@link #DATA}.
     *
     * @param contentType the content type
     * @throws IllegalArgumentException if {@code contentType} is invalid
     */
    public void setContentType(int contentType) {
        if (contentType != DATA && contentType != SIGNED_DATA) {
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }
        this.contentType = contentType;
    }

    /**
     * Sets the message-digest algorithm.
     * The default algorithm is {@code "SHA-1"}.
     *
     * @param hashAlgorithm the name of the message-digest algorithm
     * @throws NullPointerException if {@code hashAlgorithm} is {@code null}
     * @throws IllegalArgumentException if {@code hashAlgorithm} is invalid
     */
    public void setDigestAlgorithm(String hashAlgorithm) {
        this.digestAlgorithm = DoJaCryptoSupport.normalizeDigestAlgorithm(hashAlgorithm);
    }

    /**
     * Gets the message-digest algorithm set in this object.
     *
     * @return the name of the message-digest algorithm
     */
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    /**
     * Discards the data to be signed that is set in this object.
     * If called when no data to be signed is set, this method does nothing.
     * The attributes set in this object are not reset.
     */
    public void reset() {
        buffer.reset();
    }

    /**
     * Generates digital-signed data in PKCS#7 SignedData format.
     * This is equivalent to calling {@link #sign(int)} with the UIM
     * certificate entry ID.
     *
     * @return the digital-signed-data object
     * @throws SignatureException if signature generation fails
     * @throws com.nttdocomo.system.InterruptedOperationException never raised on
     *         the desktop host, but declared by the API contract
     */
    public PKCS7SignedData sign() throws SignatureException, com.nttdocomo.system.InterruptedOperationException {
        try {
            return signInternal(0);
        } catch (StoreException exception) {
            throw new SignatureException(SignatureException.UNDEFINED, exception.getMessage());
        }
    }

    /**
     * Generates digital-signed data in PKCS#7 SignedData format using the
     * specified certificate entry.
     *
     * @param certificateId the entry ID acquired by the certificate store
     * @return the digital-signed-data object
     * @throws UnsupportedOperationException if terminal-memory certificates are
     *         not supported on the host and a non-UIM entry is specified
     * @throws SignatureException if signature generation fails
     * @throws com.nttdocomo.system.InterruptedOperationException never raised on
     *         the desktop host, but declared by the API contract
     * @throws StoreException if the certificate entry does not exist
     */
    public PKCS7SignedData sign(int certificateId)
            throws SignatureException, com.nttdocomo.system.InterruptedOperationException, StoreException {
        return signInternal(certificateId);
    }

    /**
     * Adds the specified byte to the data to be signed.
     *
     * @param input the byte of data to append
     */
    public void update(byte input) {
        buffer.write(input);
    }

    /**
     * Adds the specified byte array to the data to be signed.
     *
     * @param buf the byte array of data to append
     * @throws NullPointerException if {@code buf} is {@code null}
     */
    public void update(byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        buffer.write(buf, 0, buf.length);
    }

    /**
     * Adds part of the specified byte array to the data to be signed.
     *
     * @param buf the byte array of data to append
     * @param off the start offset in the byte array
     * @param len the length in the byte array
     * @throws NullPointerException if {@code buf} is {@code null}
     * @throws ArrayIndexOutOfBoundsException if the range is invalid
     */
    public void update(byte[] buf, int off, int len) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        if (off < 0 || len < 0 || off + len > buf.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid range: off=" + off + ", len=" + len);
        }
        buffer.write(buf, off, len);
    }

    private PKCS7SignedData signInternal(int certificateId) throws SignatureException, StoreException {
        ensureDigitalSignatureAvailable();
        ensureApplicationActive();
        byte[] content = buffer.toByteArray();
        if (content.length == 0) {
            throw new IllegalStateException("No data to sign is set");
        }
        if (certificateId != 0) {
            if (certificateId >= -256 && certificateId <= -1) {
                throw new StoreException(StoreException.NOT_FOUND, "Certificate entry was not found: " + certificateId);
            }
            throw new UnsupportedOperationException(
                    "openDoJa does not support terminal-memory certificates for PKCS7Signer.sign(int)");
        }
        if (contentType == SIGNED_DATA) {
            try {
                DoJaCryptoSupport.parsePkcs7(content);
            } catch (IllegalArgumentException exception) {
                throw new SignatureException(SignatureException.ILLEGAL_CONTENT, exception.getMessage());
            }
        }
        if (!SIGNING.compareAndSet(false, true)) {
            throw new IllegalStateException("A sign operation is already in progress");
        }
        try {
            byte[] encoded = DoJaCryptoSupport.generateSignedData(content, digestAlgorithm, contentType == SIGNED_DATA);
            PKCS7SignedData signedData = new PKCS7SignedData(DoJaCryptoSupport.parsePkcs7(encoded));
            reset();
            return signedData;
        } catch (SignatureException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new SignatureException(SignatureException.ILLEGAL_CONTENT, exception.getMessage());
        } catch (java.lang.IllegalStateException exception) {
            throw new IllegalStateException(exception.getMessage());
        } finally {
            SIGNING.set(false);
        }
    }

    private static void ensureDigitalSignatureAvailable() {
        String uimVersion = Phone.getProperty(Phone.UIM_VERSION);
        if (uimVersion == null) {
            throw new IllegalStateException("UIM information could not be acquired");
        }
        try {
            if (Integer.parseInt(uimVersion) < 2) {
                throw new UnsupportedOperationException("Digital signatures require UIM Version 2 or later");
            }
        } catch (NumberFormatException exception) {
            throw new UnsupportedOperationException("Digital signatures require UIM Version 2 or later");
        }
    }

    private static void ensureApplicationActive() {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null && runtime.application() instanceof MApplication application && !application.isActive()) {
            throw new IllegalStateException("Application is inactive");
        }
    }
}
