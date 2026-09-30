package opendoja.host.security;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Minimal PKCS#7 SignedData DER codec used by the DoJa 5.1 security package.
 */
final class DoJaPkcs7Support {
    static final String OID_DATA = "1.2.840.113549.1.7.1";
    static final String OID_SIGNED_DATA = "1.2.840.113549.1.7.2";
    static final String OID_RSA_ENCRYPTION = "1.2.840.113549.1.1.1";
    static final String OID_SHA1 = "1.3.14.3.2.26";
    static final String OID_MD5 = "1.2.840.113549.2.5";
    static final String OID_SHA256 = "2.16.840.1.101.3.4.2.1";
    static final String OID_SHA384 = "2.16.840.1.101.3.4.2.2";
    static final String OID_SHA512 = "2.16.840.1.101.3.4.2.3";
    static final String OID_ATTR_CONTENT_TYPE = "1.2.840.113549.1.9.3";
    static final String OID_ATTR_MESSAGE_DIGEST = "1.2.840.113549.1.9.4";

    private static final int TAG_SEQUENCE = 0x30;
    private static final int TAG_SET = 0x31;
    private static final int TAG_INTEGER = 0x02;
    private static final int TAG_OCTET_STRING = 0x04;

    private DoJaPkcs7Support() {
    }

    static ParsedSignedData parse(byte[] encoded) {
        if (encoded == null) {
            throw new NullPointerException("data");
        }
        try {
            DerElement outer = new DerReader(encoded).readElement();
            outer.expectTag(TAG_SEQUENCE);
            DerReader outerReader = outer.reader();
            String outerOid = outerReader.readElement().asOid();
            if (!OID_SIGNED_DATA.equals(outerOid)) {
                throw new IllegalArgumentException("ContentInfo is not PKCS#7 SignedData");
            }
            DerElement signedDataWrapper = outerReader.readElement();
            signedDataWrapper.expectContextSpecific(0);
            DerElement signedDataElement = signedDataWrapper.reader().readElement();
            signedDataElement.expectTag(TAG_SEQUENCE);
            return parseSignedData(encoded.clone(), signedDataElement);
        } catch (IOException | GeneralSecurityException exception) {
            throw new IllegalArgumentException("Invalid PKCS#7 SignedData format", exception);
        }
    }

    static byte[] generate(byte[] content,
                           String digestAlgorithm,
                           boolean signedDataContent,
                           PrivateKey privateKey,
                           X509Certificate[] certificateChain)
            throws GeneralSecurityException {
        if (content == null) {
            throw new NullPointerException("content");
        }
        if (certificateChain == null || certificateChain.length == 0 || certificateChain[0] == null) {
            throw new IllegalArgumentException("certificateChain must contain at least one certificate");
        }
        String normalizedDigest = MessageDigest.getInstance(digestAlgorithm).getAlgorithm();
        String digestOid = digestOid(normalizedDigest);
        Signature signature = Signature.getInstance(signatureAlgorithm(normalizedDigest, OID_RSA_ENCRYPTION));
        signature.initSign(privateKey);
        signature.update(content);
        byte[] encryptedDigest = signature.sign();

        X509Certificate signerCertificate = certificateChain[0];
        byte[] signerInfo = sequence(
                integer(BigInteger.ONE),
                sequence(
                        signerCertificate.getIssuerX500Principal().getEncoded(),
                        integer(signerCertificate.getSerialNumber())
                ),
                algorithmIdentifier(digestOid),
                algorithmIdentifier(OID_RSA_ENCRYPTION),
                octetString(encryptedDigest)
        );

        byte[] certificates = contextSpecificConstructed(0, encodedCertificates(certificateChain));
        byte[] signedData = sequence(
                integer(BigInteger.ONE),
                setOf(algorithmIdentifier(digestOid)),
                contentInfo(signedDataContent ? OID_SIGNED_DATA : OID_DATA, content, signedDataContent),
                certificates,
                setOf(signerInfo)
        );
        return contentInfo(OID_SIGNED_DATA, signedData, true);
    }

    static void verify(ParsedSignedData data) throws GeneralSecurityException {
        if (data.signers().length == 0) {
            throw new GeneralSecurityException("No signer information is present");
        }
        for (ParsedSigner signer : data.signers()) {
            if (signer.certificate() == null) {
                throw new GeneralSecurityException("Signer certificate could not be resolved");
            }
            byte[] signedBytes = data.content();
            if (signer.authenticatedAttributesSetDer() != null) {
                if (signer.authenticatedMessageDigest() != null) {
                    MessageDigest digest = MessageDigest.getInstance(signer.digestAlgorithm());
                    byte[] expected = digest.digest(data.content() == null ? new byte[0] : data.content());
                    if (!Arrays.equals(expected, signer.authenticatedMessageDigest())) {
                        throw new GeneralSecurityException("Authenticated messageDigest does not match the content");
                    }
                }
                if (signer.authenticatedContentTypeOid() != null
                        && !signer.authenticatedContentTypeOid().equals(data.contentTypeOid())) {
                    throw new GeneralSecurityException("Authenticated contentType does not match the SignedData content");
                }
                signedBytes = signer.authenticatedAttributesSetDer();
            }
            Signature signature = Signature.getInstance(signatureAlgorithm(signer.digestAlgorithm(),
                    signer.encryptionAlgorithmOid()));
            signature.initVerify(signer.certificate().getPublicKey());
            signature.update(signedBytes == null ? new byte[0] : signedBytes);
            if (!signature.verify(signer.encryptedDigest())) {
                throw new GeneralSecurityException("PKCS#7 signature verification failed");
            }
        }
    }

    private static ParsedSignedData parseSignedData(byte[] encoded, DerElement signedDataElement)
            throws IOException, GeneralSecurityException {
        DerReader reader = signedDataElement.reader();
        reader.readElement().expectTag(TAG_INTEGER);
        reader.readElement().expectTag(TAG_SET);

        DerElement contentInfo = reader.readElement();
        contentInfo.expectTag(TAG_SEQUENCE);
        ContentInfoData contentData = parseContentInfo(contentInfo);

        X509Certificate[] certificates = null;
        DerElement next = reader.readIfAvailable();
        if (next != null && next.isContextSpecific(0)) {
            certificates = parseCertificates(next.value());
            next = reader.readIfAvailable();
        }
        if (next != null && next.isContextSpecific(1)) {
            next = reader.readIfAvailable();
        }
        if (next == null) {
            throw new IOException("signerInfos are missing");
        }
        next.expectTag(TAG_SET);
        DerReader signerReader = next.reader();
        List<ParsedSigner> signers = new ArrayList<>();
        while (signerReader.hasRemaining()) {
            signers.add(parseSigner(signerReader.readElement(), contentData.contentTypeOid(), certificates));
        }
        return new ParsedSignedData(encoded, contentData.contentTypeOid(), contentData.content(), certificates,
                signers.toArray(ParsedSigner[]::new));
    }

    private static ContentInfoData parseContentInfo(DerElement contentInfo) throws IOException {
        DerReader reader = contentInfo.reader();
        String contentTypeOid = reader.readElement().asOid();
        byte[] content = null;
        DerElement wrapper = reader.readIfAvailable();
        if (wrapper != null) {
            wrapper.expectContextSpecific(0);
            DerElement inner = wrapper.reader().readElement();
            if (OID_DATA.equals(contentTypeOid)) {
                inner.expectTag(TAG_OCTET_STRING);
                content = inner.value().clone();
            } else {
                content = inner.encoded().clone();
            }
        }
        return new ContentInfoData(contentTypeOid, content);
    }

    private static X509Certificate[] parseCertificates(byte[] encodedCertificates)
            throws IOException, GeneralSecurityException {
        DerReader reader = new DerReader(encodedCertificates);
        List<X509Certificate> certificates = new ArrayList<>();
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        while (reader.hasRemaining()) {
            DerElement element = reader.readElement();
            certificates.add((X509Certificate) factory.generateCertificate(
                    new ByteArrayInputStream(element.encoded())));
        }
        return certificates.isEmpty() ? null : certificates.toArray(X509Certificate[]::new);
    }

    private static ParsedSigner parseSigner(DerElement signerInfo,
                                            String contentTypeOid,
                                            X509Certificate[] certificates)
            throws IOException {
        signerInfo.expectTag(TAG_SEQUENCE);
        DerReader reader = signerInfo.reader();
        reader.readElement().expectTag(TAG_INTEGER);

        DerElement issuerAndSerial = reader.readElement();
        issuerAndSerial.expectTag(TAG_SEQUENCE);
        DerReader issuerReader = issuerAndSerial.reader();
        DerElement issuerName = issuerReader.readElement();
        X500Principal issuerPrincipal = new X500Principal(issuerName.encoded());
        BigInteger serialNumber = issuerReader.readElement().asInteger();

        String digestAlgorithm = digestName(reader.readElement().algorithmOid());
        byte[] authenticatedAttributesSetDer = null;
        String authenticatedContentTypeOid = null;
        byte[] authenticatedMessageDigest = null;

        DerElement next = reader.readElement();
        if (next.isContextSpecific(0)) {
            authenticatedAttributesSetDer = wrapSet(next.value());
            DerReader attributesReader = new DerReader(next.value());
            while (attributesReader.hasRemaining()) {
                ParsedAttribute attribute = parseAttribute(attributesReader.readElement());
                if (OID_ATTR_CONTENT_TYPE.equals(attribute.oid())) {
                    authenticatedContentTypeOid = attribute.contentTypeOid();
                } else if (OID_ATTR_MESSAGE_DIGEST.equals(attribute.oid())) {
                    authenticatedMessageDigest = attribute.messageDigest();
                }
            }
            next = reader.readElement();
        }

        String encryptionAlgorithmOid = next.algorithmOid();
        byte[] encryptedDigest = reader.readElement().asOctetString();
        X509Certificate certificate = findCertificate(certificates, issuerPrincipal, serialNumber);
        return new ParsedSigner(
                issuerPrincipal,
                issuerPrincipal.getName(),
                DoJaCryptoSupport.hex(serialNumber),
                digestAlgorithm,
                encryptionAlgorithmOid,
                encryptedDigest,
                authenticatedAttributesSetDer,
                authenticatedContentTypeOid == null ? contentTypeOid : authenticatedContentTypeOid,
                authenticatedMessageDigest,
                certificate
        );
    }

    private static ParsedAttribute parseAttribute(DerElement attribute) throws IOException {
        attribute.expectTag(TAG_SEQUENCE);
        DerReader reader = attribute.reader();
        String oid = reader.readElement().asOid();
        DerElement values = reader.readElement();
        values.expectTag(TAG_SET);
        DerElement value = values.reader().readElement();
        String contentTypeOid = null;
        byte[] messageDigest = null;
        if (OID_ATTR_CONTENT_TYPE.equals(oid)) {
            contentTypeOid = value.asOid();
        } else if (OID_ATTR_MESSAGE_DIGEST.equals(oid)) {
            messageDigest = value.asOctetString();
        }
        return new ParsedAttribute(oid, contentTypeOid, messageDigest);
    }

    private static X509Certificate findCertificate(X509Certificate[] certificates,
                                                   X500Principal issuerPrincipal,
                                                   BigInteger serialNumber) {
        if (certificates == null) {
            return null;
        }
        for (X509Certificate certificate : certificates) {
            if (certificate != null
                    && issuerPrincipal.equals(certificate.getIssuerX500Principal())
                    && serialNumber.equals(certificate.getSerialNumber())) {
                return certificate;
            }
        }
        return null;
    }

    private static byte[] encodedCertificates(X509Certificate[] certificateChain) throws GeneralSecurityException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (X509Certificate certificate : certificateChain) {
            if (certificate != null) {
                output.writeBytes(certificate.getEncoded());
            }
        }
        return output.toByteArray();
    }

    private static byte[] contentInfo(String contentTypeOid, byte[] content, boolean contentIsEncoded) {
        byte[] contentValue = contentIsEncoded ? content.clone() : octetString(content);
        return sequence(oid(contentTypeOid), explicitContext(0, contentValue));
    }

    private static byte[] algorithmIdentifier(String oid) {
        return sequence(oid(oid), nullElement());
    }

    private static byte[] integer(BigInteger value) {
        return encode(TAG_INTEGER, value.toByteArray());
    }

    private static byte[] octetString(byte[] value) {
        return encode(TAG_OCTET_STRING, value.clone());
    }

    private static byte[] nullElement() {
        return encode(0x05, new byte[0]);
    }

    private static byte[] oid(String dotted) {
        String[] parts = dotted.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid OID: " + dotted);
        }
        int first = Integer.parseInt(parts[0]);
        int second = Integer.parseInt(parts[1]);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(first * 40 + second);
        for (int i = 2; i < parts.length; i++) {
            writeBase128(output, new BigInteger(parts[i]));
        }
        return encode(0x06, output.toByteArray());
    }

    private static void writeBase128(ByteArrayOutputStream output, BigInteger value) {
        if (value.signum() < 0) {
            throw new IllegalArgumentException("OID component must be non-negative");
        }
        if (value.equals(BigInteger.ZERO)) {
            output.write(0);
            return;
        }
        byte[] stack = new byte[16];
        int size = 0;
        BigInteger remaining = value;
        BigInteger base = BigInteger.valueOf(128);
        while (remaining.signum() > 0) {
            BigInteger[] divRem = remaining.divideAndRemainder(base);
            stack[size++] = (byte) divRem[1].intValue();
            remaining = divRem[0];
        }
        for (int i = size - 1; i >= 0; i--) {
            int b = stack[i] & 0x7F;
            if (i != 0) {
                b |= 0x80;
            }
            output.write(b);
        }
    }

    private static byte[] sequence(byte[]... elements) {
        return encode(TAG_SEQUENCE, concat(elements));
    }

    private static byte[] setOf(byte[]... elements) {
        return encode(TAG_SET, concat(elements));
    }

    private static byte[] explicitContext(int tagNumber, byte[] encodedInner) {
        return encode(0xA0 | tagNumber, encodedInner.clone());
    }

    private static byte[] contextSpecificConstructed(int tagNumber, byte[] value) {
        return encode(0xA0 | tagNumber, value.clone());
    }

    private static byte[] wrapSet(byte[] contents) {
        return encode(TAG_SET, contents.clone());
    }

    private static byte[] concat(byte[]... arrays) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] array : arrays) {
            if (array != null) {
                output.writeBytes(array);
            }
        }
        return output.toByteArray();
    }

    private static byte[] encode(int tag, byte[] value) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(tag);
        writeLength(output, value.length);
        output.writeBytes(value);
        return output.toByteArray();
    }

    private static void writeLength(ByteArrayOutputStream output, int length) {
        if (length < 0x80) {
            output.write(length);
            return;
        }
        int tmp = length;
        byte[] bytes = new byte[4];
        int size = 0;
        while (tmp > 0) {
            bytes[size++] = (byte) (tmp & 0xFF);
            tmp >>>= 8;
        }
        output.write(0x80 | size);
        for (int i = size - 1; i >= 0; i--) {
            output.write(bytes[i]);
        }
    }

    private static String digestOid(String algorithm) {
        return switch (algorithm) {
            case "MD5" -> OID_MD5;
            case "SHA-1" -> OID_SHA1;
            case "SHA-256" -> OID_SHA256;
            case "SHA-384" -> OID_SHA384;
            case "SHA-512" -> OID_SHA512;
            default -> throw new IllegalArgumentException("Unsupported digest algorithm: " + algorithm);
        };
    }

    private static String digestName(String oid) {
        return switch (oid) {
            case OID_MD5 -> "MD5";
            case OID_SHA1 -> "SHA-1";
            case OID_SHA256 -> "SHA-256";
            case OID_SHA384 -> "SHA-384";
            case OID_SHA512 -> "SHA-512";
            default -> throw new IllegalArgumentException("Unsupported digest OID: " + oid);
        };
    }

    private static String signatureAlgorithm(String digestAlgorithm, String encryptionAlgorithmOid) {
        return switch (encryptionAlgorithmOid) {
            case OID_RSA_ENCRYPTION -> digestAlgorithm.replace("-", "") + "withRSA";
            case "1.2.840.113549.1.1.4" -> "MD5withRSA";
            case "1.2.840.113549.1.1.5" -> "SHA1withRSA";
            case "1.2.840.113549.1.1.11" -> "SHA256withRSA";
            case "1.2.840.113549.1.1.12" -> "SHA384withRSA";
            case "1.2.840.113549.1.1.13" -> "SHA512withRSA";
            default -> throw new IllegalArgumentException("Unsupported signature algorithm OID: " + encryptionAlgorithmOid);
        };
    }

    record ParsedSignedData(byte[] encoded,
                            String contentTypeOid,
                            byte[] content,
                            X509Certificate[] certificates,
                            ParsedSigner[] signers) {
    }

    record ParsedSigner(X500Principal issuerPrincipal,
                        String issuerDn,
                        String serialNumberHex,
                        String digestAlgorithm,
                        String encryptionAlgorithmOid,
                        byte[] encryptedDigest,
                        byte[] authenticatedAttributesSetDer,
                        String authenticatedContentTypeOid,
                        byte[] authenticatedMessageDigest,
                        X509Certificate certificate) {
    }

    private record ContentInfoData(String contentTypeOid, byte[] content) {
    }

    private record ParsedAttribute(String oid, String contentTypeOid, byte[] messageDigest) {
    }

    private static final class DerReader {
        private final byte[] data;
        private int position;

        private DerReader(byte[] data) {
            this.data = data;
        }

        private boolean hasRemaining() {
            return position < data.length;
        }

        private DerElement readIfAvailable() throws IOException {
            return hasRemaining() ? readElement() : null;
        }

        private DerElement readElement() throws IOException {
            if (!hasRemaining()) {
                throw new IOException("Unexpected end of DER data");
            }
            int tag = data[position++] & 0xFF;
            int length = readLength();
            if (position + length > data.length) {
                throw new IOException("DER length exceeds available data");
            }
            byte[] value = Arrays.copyOfRange(data, position, position + length);
            int start = position;
            position += length;
            byte[] encoded = Arrays.copyOfRange(data, start - encodedLengthBytes(length) - 1, position);
            return new DerElement(tag, value, encoded);
        }

        private int readLength() throws IOException {
            int first = data[position++] & 0xFF;
            if ((first & 0x80) == 0) {
                return first;
            }
            int size = first & 0x7F;
            if (size == 0 || size > 4 || position + size > data.length) {
                throw new IOException("Unsupported DER length");
            }
            int length = 0;
            for (int i = 0; i < size; i++) {
                length = (length << 8) | (data[position++] & 0xFF);
            }
            return length;
        }

        private int encodedLengthBytes(int length) {
            if (length < 0x80) {
                return 1;
            }
            int size = 0;
            int tmp = length;
            while (tmp > 0) {
                size++;
                tmp >>>= 8;
            }
            return 1 + size;
        }
    }

    private record DerElement(int tag, byte[] value, byte[] encoded) {
        private void expectTag(int expectedTag) throws IOException {
            if (tag != expectedTag) {
                throw new IOException("Unexpected DER tag: " + tag);
            }
        }

        private void expectContextSpecific(int tagNumber) throws IOException {
            int expected = 0xA0 | tagNumber;
            if (tag != expected) {
                throw new IOException("Unexpected DER context-specific tag: " + tag);
            }
        }

        private boolean isContextSpecific(int tagNumber) {
            return tag == (0xA0 | tagNumber);
        }

        private DerReader reader() {
            return new DerReader(value);
        }

        private String asOid() throws IOException {
            expectTag(0x06);
            if (value.length == 0) {
                throw new IOException("Invalid empty OID");
            }
            List<String> parts = new ArrayList<>();
            int first = value[0] & 0xFF;
            parts.add(Integer.toString(first / 40));
            parts.add(Integer.toString(first % 40));
            int index = 1;
            while (index < value.length) {
                BigInteger component = BigInteger.ZERO;
                int b;
                do {
                    b = value[index++] & 0xFF;
                    component = component.shiftLeft(7).or(BigInteger.valueOf(b & 0x7F));
                } while ((b & 0x80) != 0 && index < value.length);
                parts.add(component.toString());
            }
            return String.join(".", parts);
        }

        private BigInteger asInteger() throws IOException {
            expectTag(TAG_INTEGER);
            return new BigInteger(value);
        }

        private byte[] asOctetString() throws IOException {
            expectTag(TAG_OCTET_STRING);
            return value.clone();
        }

        private String algorithmOid() throws IOException {
            expectTag(TAG_SEQUENCE);
            return reader().readElement().asOid();
        }
    }
}
