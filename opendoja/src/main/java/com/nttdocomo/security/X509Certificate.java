package com.nttdocomo.security;

import opendoja.host.security.DoJaCryptoSupport;

import java.util.Date;

/**
 * Defines an X.509 certificate.
 */
public class X509Certificate {
    /** Represents the common-name attribute ("CN"). */
    public static final String COMMON_NAME = "CN";

    /** Represents the organization-unit attribute ("OU"). */
    public static final String ORGANIZATION_UNIT = "OU";

    /** Represents the organization attribute ("O"). */
    public static final String ORGANIZATION = "O";

    /** Represents the country attribute ("C"). */
    public static final String COUNTRY = "C";

    private final java.security.cert.X509Certificate certificate;

    /**
     * Creates a certificate object.
     */
    protected X509Certificate() {
        this.certificate = null;
    }

    X509Certificate(java.security.cert.X509Certificate certificate) {
        this.certificate = certificate;
    }

    /**
     * Gets the value set in the certificate subject (distinguished name of the
     * subject).
     * Values can be acquired for {@link #COMMON_NAME},
     * {@link #ORGANIZATION}, and {@link #COUNTRY}.
     *
     * @param attr the attribute
     * @return the value of the specified attribute
     * @throws NullPointerException if {@code attr} is {@code null}
     * @throws IllegalArgumentException if {@code attr} is invalid
     */
    public String getSubject(String attr) {
        validateSubjectAttribute(attr);
        return DoJaCryptoSupport.distinguishedNameAttribute(javaCertificate().getSubjectX500Principal().getName(), attr);
    }

    /**
     * Gets the value set in the certificate issuer (issuer distinguished
     * name).
     * Values can be acquired for {@link #COMMON_NAME},
     * {@link #ORGANIZATION_UNIT}, and {@link #ORGANIZATION}.
     *
     * @param attr the attribute
     * @return the value of the specified attribute
     * @throws NullPointerException if {@code attr} is {@code null}
     * @throws IllegalArgumentException if {@code attr} is invalid
     */
    public String getIssuer(String attr) {
        validateIssuerAttribute(attr);
        return DoJaCryptoSupport.distinguishedNameAttribute(javaCertificate().getIssuerX500Principal().getName(), attr);
    }

    /**
     * Gets the notAfter date from the certificate validity period.
     *
     * @return the end date of the certificate validity period
     */
    public Date getNotAfter() {
        return new Date(javaCertificate().getNotAfter().getTime());
    }

    /**
     * Gets the certificate serial number.
     * The returned string is the serial number in hexadecimal notation without
     * a leading {@code 0x}.
     *
     * @return the certificate serial number
     */
    public String getSerialNumber() {
        return DoJaCryptoSupport.hex(javaCertificate().getSerialNumber());
    }

    java.security.cert.X509Certificate javaCertificate() {
        if (certificate == null) {
            throw new IllegalStateException("Certificate backing data is not available");
        }
        return certificate;
    }

    static X509Certificate wrap(java.security.cert.X509Certificate certificate) {
        return certificate == null ? null : new X509Certificate(certificate);
    }

    private static void validateSubjectAttribute(String attr) {
        if (attr == null) {
            throw new NullPointerException("attr");
        }
        if (!COMMON_NAME.equals(attr) && !ORGANIZATION.equals(attr) && !COUNTRY.equals(attr)) {
            throw new IllegalArgumentException("Unsupported subject attribute: " + attr);
        }
    }

    private static void validateIssuerAttribute(String attr) {
        if (attr == null) {
            throw new NullPointerException("attr");
        }
        if (!COMMON_NAME.equals(attr) && !ORGANIZATION_UNIT.equals(attr) && !ORGANIZATION.equals(attr)) {
            throw new IllegalArgumentException("Unsupported issuer attribute: " + attr);
        }
    }
}
