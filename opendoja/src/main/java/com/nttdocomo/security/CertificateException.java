package com.nttdocomo.security;

/**
 * Defines exceptions related to certificates.
 */
public class CertificateException extends Exception {
    /** Indicates that the status is undefined (=0). */
    public static final int UNDEFINED = 0;

    /**
     * Indicates that the terminal does not hold the root certificate
     * corresponding to the signature to be verified, that the certificate is
     * outside its validity period, or that it has been disabled (=1).
     */
    public static final int UNTRUSTED_CA = 1;

    /** Indicates that the certificate is outside its validity period (=2). */
    public static final int INVALID = 2;

    private final int status;

    /**
     * Creates a certificate-exception object without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public CertificateException() {
        this(UNDEFINED);
    }

    /**
     * Creates a certificate-exception object with a status value.
     *
     * @param status the status value to set
     */
    public CertificateException(int status) {
        this(status, null);
    }

    /**
     * Creates a certificate-exception object with a status value and a detail
     * message.
     *
     * @param status the status value to set
     * @param msg the detail message
     */
    public CertificateException(int status, String msg) {
        super(msg);
        this.status = status;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }
}
