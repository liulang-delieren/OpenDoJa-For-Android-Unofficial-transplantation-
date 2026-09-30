package com.nttdocomo.security;

import opendoja.host.security.DoJaCryptoSupport;

/**
 * Defines signer information contained in PKCS#7 SignedData.
 * Objects of this class are obtained by calling
 * {@link PKCS7SignedData#getSignerInfos()}.
 */
public class PKCS7SignerInfo {
    private final String issuerDn;
    private final String serialNumber;

    PKCS7SignerInfo(DoJaCryptoSupport.SignerData signerData) {
        this.issuerDn = signerData == null ? null : signerData.issuerDn();
        this.serialNumber = signerData == null ? null : signerData.serialNumberHex();
    }

    /**
     * Gets the value set in the issuer (issuer distinguished name) of the
     * signer information.
     * Values can be acquired for {@link X509Certificate#COMMON_NAME},
     * {@link X509Certificate#ORGANIZATION_UNIT}, and
     * {@link X509Certificate#ORGANIZATION}.
     *
     * @param attr the attribute
     * @return the value of the specified attribute
     * @throws NullPointerException if {@code attr} is {@code null}
     * @throws IllegalArgumentException if {@code attr} is invalid
     */
    public String getIssuer(String attr) {
        if (attr == null) {
            throw new NullPointerException("attr");
        }
        if (!X509Certificate.COMMON_NAME.equals(attr)
                && !X509Certificate.ORGANIZATION_UNIT.equals(attr)
                && !X509Certificate.ORGANIZATION.equals(attr)) {
            throw new IllegalArgumentException("Unsupported issuer attribute: " + attr);
        }
        return DoJaCryptoSupport.distinguishedNameAttribute(issuerDn, attr);
    }

    /**
     * Gets the serial number of the signer information.
     * The returned string is the serial number in hexadecimal notation without
     * a leading {@code 0x}.
     *
     * @return the serial number of the signer information
     */
    public String getSerialNumber() {
        return serialNumber;
    }
}
