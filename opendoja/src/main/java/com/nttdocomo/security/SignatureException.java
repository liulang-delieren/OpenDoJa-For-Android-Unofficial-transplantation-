package com.nttdocomo.security;

/**
 * Defines digital-signature exceptions.
 */
public class SignatureException extends Exception {
    /** Indicates that the status is undefined (=0). */
    public static final int UNDEFINED = 0;

    /** Indicates that PIN2 has become blocked (=1). */
    public static final int PIN2_BLOCKED = 1;

    /** Indicates that PIN2 was already blocked (=2). */
    public static final int PIN2_ALREADY_BLOCKED = 2;

    /** Indicates that PUK2 has become blocked (=3). */
    public static final int PUK2_BLOCKED = 3;

    /** Indicates that PUK2 was already blocked (=4). */
    public static final int PUK2_ALREADY_BLOCKED = 4;

    /** Indicates that the user canceled PIN2 entry (=5). */
    public static final int PIN2_CANCELED = 5;

    /** Indicates that the user canceled PUK2 entry (=6). */
    public static final int PUK2_CANCELED = 6;

    /** Indicates that digital-signature generation failed (=7). */
    public static final int SIGN_ERROR = 7;

    /** Indicates that the content format is invalid (=8). */
    public static final int ILLEGAL_CONTENT = 8;

    /**
     * Indicates that terminal security-code input or fingerprint
     * authentication was rejected (=9).
     */
    public static final int SECURITY_CODE_REJECTED = 9;

    /**
     * Indicates that generating the encryptedDigest value of PKCS#7 signer
     * information failed (=10).
     */
    public static final int ENCRYPTED_DIGEST_ERROR = 10;

    private final int status;

    /**
     * Creates a signature-exception object without a detail message.
     * The status is set to {@link #UNDEFINED}.
     */
    public SignatureException() {
        this(UNDEFINED);
    }

    /**
     * Creates a signature-exception object with a status value.
     *
     * @param status the status value to set
     */
    public SignatureException(int status) {
        this(status, null);
    }

    /**
     * Creates a signature-exception object with a status value and a detail
     * message.
     *
     * @param status the status value to set
     * @param msg the detail message
     */
    public SignatureException(int status, String msg) {
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
