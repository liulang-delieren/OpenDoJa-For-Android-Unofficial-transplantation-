package com.nttdocomo.security;

import com.nttdocomo.lang.IllegalStateException;
import com.nttdocomo.lang.UnsupportedOperationException;
import com.nttdocomo.util.Phone;
import opendoja.host.security.DoJaCryptoSupport;

/**
 * Defines PKCS#7 SignedData.
 */
public class PKCS7SignedData {
    private final DoJaCryptoSupport.Pkcs7Data pkcs7Data;

    /**
     * Creates a {@code PKCS7SignedData} object from the specified binary data.
     * The binary data must be ASN.1 data encoded according to DER and compliant
     * with PKCS#7 SignedData format.
     *
     * @param data the binary data of the digital-signed data
     * @throws NullPointerException if {@code data} is {@code null}
     * @throws IllegalArgumentException if the specified data format is invalid
     * @throws IllegalStateException if UIM information could not be acquired
     * @throws UnsupportedOperationException if the inserted UIM version is not
     *         Version 2 or later
     */
    public PKCS7SignedData(byte[] data) {
        this(data, false);
    }

    PKCS7SignedData(byte[] data, boolean ignored) {
        ensureDigitalSignatureAvailable();
        try {
            this.pkcs7Data = DoJaCryptoSupport.parsePkcs7(data);
        } catch (java.lang.IllegalStateException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    PKCS7SignedData(DoJaCryptoSupport.Pkcs7Data pkcs7Data) {
        ensureDigitalSignatureAvailable();
        this.pkcs7Data = pkcs7Data;
    }

    /**
     * Returns the digital-signed data as binary data.
     * The binary data returned is ASN.1 data encoded according to DER.
     *
     * @return the binary data of the digital-signed data as a byte array
     */
    public byte[] getEncoded() {
        return pkcs7Data.encoded();
    }

    /**
     * Gets the inner content contained in the digital-signed data as binary
     * data.
     *
     * @return the content as a byte array
     */
    public byte[] getContent() {
        return pkcs7Data.content();
    }

    /**
     * Gets all certificates contained in the digital-signed data.
     *
     * @return all certificates, or {@code null} if there are no certificates
     */
    public X509Certificate[] getCertificates() {
        java.security.cert.X509Certificate[] certificates = pkcs7Data.certificates();
        if (certificates == null || certificates.length == 0) {
            return null;
        }
        X509Certificate[] wrapped = new X509Certificate[certificates.length];
        for (int i = 0; i < certificates.length; i++) {
            wrapped[i] = X509Certificate.wrap(certificates[i]);
        }
        return wrapped;
    }

    /**
     * Gets the signer information contained in the digital-signed data.
     *
     * @return all signer information, or {@code null} if none is present
     */
    public PKCS7SignerInfo[] getSignerInfos() {
        DoJaCryptoSupport.SignerData[] signers = pkcs7Data.signers();
        if (signers == null || signers.length == 0) {
            return null;
        }
        PKCS7SignerInfo[] infos = new PKCS7SignerInfo[signers.length];
        for (int i = 0; i < signers.length; i++) {
            infos[i] = new PKCS7SignerInfo(signers[i]);
        }
        return infos;
    }

    /**
     * Verifies the digital-signed data.
     * If verification succeeds, {@code true} is returned. If verification
     * fails, an exception is raised according to the reason for the failure.
     *
     * @return {@code true} if verification succeeds
     * @throws CertificateException if certificate validation fails
     * @throws SignatureException if the verification result cannot be obtained
     *         or the verification fails
     */
    public boolean verify() throws SignatureException, CertificateException {
        try {
            return DoJaCryptoSupport.verifyPkcs7(pkcs7Data);
        } catch (java.lang.IllegalStateException exception) {
            throw new IllegalStateException(exception.getMessage());
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
}
