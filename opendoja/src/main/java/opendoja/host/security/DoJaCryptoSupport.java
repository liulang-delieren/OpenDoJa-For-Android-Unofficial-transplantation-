package opendoja.host.security;

import com.nttdocomo.security.CertificateException;
import com.nttdocomo.security.CipherException;
import com.nttdocomo.security.SignatureException;
import com.nttdocomo.security.SymmetricCipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import opendoja.compat.naming.InvalidNameException;
import opendoja.compat.naming.LdapName;
import opendoja.compat.naming.Rdn;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.*;

/**
 * Host-side cryptographic helpers used by the DoJa 5.1 security package.
 */
public final class DoJaCryptoSupport {
    private static final byte[] WRAPPED_SESSION_KEY_MAGIC = "ODJCSK1".getBytes(StandardCharsets.US_ASCII);
    private static final String HOST_PRIVATE_KEY_DER_BASE64 =
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCk+W5OOjW7l6r0bVsbcjnt17iqugyECUvNYeYoIyPZfrewnOJXE6lSkk34JlSIexvB4nA1jY4GfhIfzs7g5eaxrxI+jIybCSc8+sUjvv+fT4FMYs0oX0zGkE3XMy7JZ7/AyWjewo/84XWefNlp4VB0jCLtzURUjE2ZctiSDneVH8AI8HVjIGKtH1jbxc4uAGqUfAYV4vSRO//g++xzlEfJH0wgPAF9BwU3bL3BbSWevCPxQF15NL9fS2K9M51SpQ2Rwm1anCsEpDqzaSgJ5Y2P7aPipv/U6D5dJVhcmGR4KLp8FqmujIfLyr7ku6SkLg/8TWHACOI0B6cyaAayXhEvAgMBAAECggEAKyPEmfUzztnCzQb5n4w3pL/X3OEo27AYn9zdVBqYScwOmcL0lwOfr+VtORpA2a2jnQgrAn4BKSZ3c9TfGUVVFZqrwSp8rB/xIEuNGKNd8dlW/NQLRObiyzu59y+9qNIV/QJOB45GG8ETQj3wlnTiVo+8pIWcPCWyNxGmm07oK1jFTX4wUHI+cAdUcfxergY7K0ehNYJHKqu45g4l7yHv3UM6YWqe0POLuvo+9vxFyHOORtlyHnzekAjYbCJsYAgajMubR535y5UDwjQGFs5Tse65L9On9jNlBWbvaoVTgaASo6Pd3Tno+zjWKUqHehVJMYYA169fkWZb3AcK1sFDbQKBgQDotx1GqUmp2v6G0cdLACGJe6Chjj4LLZGwEbVdcyniSoHeMFnvJBEf2aDT0mLA26xN5WDVzQldpMWXC+4t7Vl04VH5hNAAN7MgUaG9kTBm2ZodgKUdCGfkPxtcMf4plz5bljNiJAhDY9vLimNBrJ0wY+B6KO+X11UqiaZu7vciwwKBgQC1eyp9lOVuHog21+rUQbtmDXhDqB6dzcWFMaYtSbfjLCfjPExlmnc5+ZID1TG+Zsy5RvbuSkYAIfKACgzR6ZgSAJJj97fJHNVEulmBVEZsW1UoqyVrTAWSwf/I+2UOyi+oGyBzVJgG7yPVNq2NNcVzeDOB47ZrZp/mc6sEGqsZJQKBgDPANOR31QfOyXdVw0nsd4jJU0laBnYZ5iIPbhOf/ppyEztD6VwC20QH83nneUqGD2UrM0OYWxLWB5K1dnwEIjaZvM8ON+s0d8MiJCOx29+jWGBjMVSpf+EEH6N7AphhJ97aIgcbGDNUS5aR6cy7BCG0tEC2RcGwgyH4hmh4/8BdAoGAfJBwhsgHfkEYr8QeG9iOPyrprE6dzSSq+ZQtgpJB1Hy/WBpQOcD/KtzhWx4fSZgX1ugdn0t9pqOmZjn/uKkERv77fABQtorC26youLtWpxM9bW+jfHUush/UaGgdYjxm35TW/jJHMyM7TiJ8lxscTvhnKjVZRXioMi6caHHBlL0CgYAHORYa+e6p48TPAlbO4tS89snU2c0kER5RFmTcbROOb95uoFn/fkEINookuiCMa/4Son9oRahSAk0BHDV7P48TqPmBAlRyT20asTuOyo3+LERRCYB9sI+7AVfEFdrvA88C0MXGuSngfUuUYI4ZXT6DIl8hqdgbsH8gg7D1qJvlgw==";
    private static final String HOST_CERTIFICATE_DER_BASE64 =
            "MIIDWTCCAkGgAwIBAgIUF6jSmQR20nPmdmQNUjxlxrjXrUgwDQYJKoZIhvcNAQELBQAwPDEaMBgGA1UEAwwRb3BlbkRvSmEgSG9zdCBVSU0xETAPBgNVBAoMCG9wZW5Eb0phMQswCQYDVQQGEwJVUzAeFw0yNjAzMjIyMTMxMjZaFw0zNjAzMTkyMTMxMjZaMDwxGjAYBgNVBAMMEW9wZW5Eb0phIEhvc3QgVUlNMREwDwYDVQQKDAhvcGVuRG9KYTELMAkGA1UEBhMCVVMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCk+W5OOjW7l6r0bVsbcjnt17iqugyECUvNYeYoIyPZfrewnOJXE6lSkk34JlSIexvB4nA1jY4GfhIfzs7g5eaxrxI+jIybCSc8+sUjvv+fT4FMYs0oX0zGkE3XMy7JZ7/AyWjewo/84XWefNlp4VB0jCLtzURUjE2ZctiSDneVH8AI8HVjIGKtH1jbxc4uAGqUfAYV4vSRO//g++xzlEfJH0wgPAF9BwU3bL3BbSWevCPxQF15NL9fS2K9M51SpQ2Rwm1anCsEpDqzaSgJ5Y2P7aPipv/U6D5dJVhcmGR4KLp8FqmujIfLyr7ku6SkLg/8TWHACOI0B6cyaAayXhEvAgMBAAGjUzBRMB0GA1UdDgQWBBRiEaAZFgE2pWEKXn+bDYO7WW70xDAfBgNVHSMEGDAWgBRiEaAZFgE2pWEKXn+bDYO7WW70xDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQA/NYNvElzIca+DpviBLokZWhOzWmdeIplT5mCxiF2GVaDdeu7m8XB4ovtQTtiSN5qG8LIeeKlT1tACv2G0ktxW0ZwTkT481a2vIprcLaEBzputD9hudWRdPyR0S8/klcdv257379lGaoWOtwZG+iqr/QrJsZIZUGtrRtuS0BO/Vk9sLLUrhRDJGX3n709s1wNtx18nDVSdgiaVkxusLSE0dqjiBl11CsEt/G2T6ejF7iVBEpGsJ1AS+ZwEbcYpThU/ObgnMFfhih9XyQ41xHpqIk624kYw47s7328GjvfPa2cMaCWMeaKxBEm34FyToRehDe3T8Rzh6kXFT9sYuwvV";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static volatile HostIdentity hostIdentity;
    private static volatile Set<TrustAnchor> trustAnchors;

    private DoJaCryptoSupport() {
    }

    public static AlgorithmSpec algorithmSpec(int cipher) {
        return switch (cipher) {
            case SymmetricCipher.DES_CBC_PKCS7PADDING ->
                    new AlgorithmSpec(cipher, "DES", "DES/CBC/PKCS5Padding", 8, 8);
            case SymmetricCipher.DES_EDE_CBC_PKCS7PADDING ->
                    new AlgorithmSpec(cipher, "DESede", "DESede/CBC/PKCS5Padding", 24, 8);
            case SymmetricCipher.AES_128_CBC_PKCS7PADDING ->
                    new AlgorithmSpec(cipher, "AES", "AES/CBC/PKCS5Padding", 16, 16);
            case SymmetricCipher.AES_192_CBC_PKCS7PADDING ->
                    new AlgorithmSpec(cipher, "AES", "AES/CBC/PKCS5Padding", 24, 16);
            case SymmetricCipher.AES_256_CBC_PKCS7PADDING ->
                    new AlgorithmSpec(cipher, "AES", "AES/CBC/PKCS5Padding", 32, 16);
            default -> throw new IllegalArgumentException("Unsupported cipher: " + cipher);
        };
    }

    public static byte[] clone(byte[] data) {
        return data == null ? null : data.clone();
    }

    public static byte[] randomBytes(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("byteSize must be positive");
        }
        byte[] random = new byte[size];
        SECURE_RANDOM.nextBytes(random);
        return random;
    }

    public static SecretKey derivePbeKey(byte[] password, byte[] salt, int iterationCount, AlgorithmSpec wrappingSpec)
            throws GeneralSecurityException {
        char[] chars = new String(password, StandardCharsets.ISO_8859_1).toCharArray();
        PBEKeySpec keySpec = new PBEKeySpec(chars, salt, iterationCount, wrappingSpec.keyLengthBytes() * 8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), wrappingSpec.keyAlgorithm());
    }

    public static javax.crypto.Cipher newCipher(AlgorithmSpec spec, SecretKey key, byte[] iv, boolean encrypt)
            throws GeneralSecurityException {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(spec.transformation());
        cipher.init(encrypt ? javax.crypto.Cipher.ENCRYPT_MODE : javax.crypto.Cipher.DECRYPT_MODE,
                key,
                new IvParameterSpec(iv));
        return cipher;
    }

    public static byte[] wrapSessionKey(byte[] sessionKey, int sessionCipher) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.writeBytes(WRAPPED_SESSION_KEY_MAGIC);
        output.write(sessionCipher);
        output.writeBytes(sessionKey);
        return output.toByteArray();
    }

    public static WrappedSessionKey unwrapSessionKey(byte[] wrapped) throws CipherException {
        if (wrapped == null
                || wrapped.length < WRAPPED_SESSION_KEY_MAGIC.length + 2
                || !Arrays.equals(Arrays.copyOf(wrapped, WRAPPED_SESSION_KEY_MAGIC.length), WRAPPED_SESSION_KEY_MAGIC)) {
            throw new CipherException(CipherException.BAD_PADDING, "Invalid wrapped session key");
        }
        int sessionCipher = wrapped[WRAPPED_SESSION_KEY_MAGIC.length] & 0xFF;
        byte[] sessionKey = Arrays.copyOfRange(wrapped, WRAPPED_SESSION_KEY_MAGIC.length + 1, wrapped.length);
        try {
            AlgorithmSpec spec = algorithmSpec(sessionCipher);
            if (sessionKey.length != spec.keyLengthBytes()) {
                throw new CipherException(CipherException.BAD_PADDING, "Wrapped session key length is invalid");
            }
        } catch (IllegalArgumentException exception) {
            throw new CipherException(CipherException.BAD_PADDING, exception.getMessage());
        }
        return new WrappedSessionKey(sessionCipher, sessionKey);
    }

    public static String normalizeDigestAlgorithm(String digestAlgorithm) {
        if (digestAlgorithm == null) {
            throw new NullPointerException("hashAlgorithm");
        }
        try {
            return MessageDigest.getInstance(digestAlgorithm).getAlgorithm();
        } catch (GeneralSecurityException exception) {
            throw new IllegalArgumentException("Unsupported digest algorithm: " + digestAlgorithm, exception);
        }
    }

    public static String signatureAlgorithmForDigest(String digestAlgorithm) {
        String normalized = normalizeDigestAlgorithm(digestAlgorithm).replace("-", "");
        return normalized + "withRSA";
    }

    public static HostIdentity hostIdentity() {
        HostIdentity cached = hostIdentity;
        if (cached != null) {
            return cached;
        }
        synchronized (DoJaCryptoSupport.class) {
            if (hostIdentity == null) {
                hostIdentity = createHostIdentity();
            }
            return hostIdentity;
        }
    }

    public static Pkcs7Data parsePkcs7(byte[] encoded) {
        if (encoded == null) {
            throw new NullPointerException("data");
        }
        try {
            DoJaPkcs7Support.ParsedSignedData parsed = DoJaPkcs7Support.parse(encoded);
            X509Certificate[] certificates = parsed.certificates();
            DoJaPkcs7Support.ParsedSigner[] parsedSigners = parsed.signers();
            SignerData[] signerData = parsedSigners == null ? null : new SignerData[parsedSigners.length];
            if (parsedSigners != null) {
                for (int i = 0; i < parsedSigners.length; i++) {
                    DoJaPkcs7Support.ParsedSigner signer = parsedSigners[i];
                    signerData[i] = new SignerData(
                            signer,
                            signer.issuerDn(),
                            signer.serialNumberHex(),
                            signer.certificate()
                    );
                }
            }
            return new Pkcs7Data(parsed, encoded.clone(), clone(parsed.content()), certificates, signerData);
        } catch (IllegalArgumentException exception) {
            throw exception;
        }
    }

    public static boolean verifyPkcs7(Pkcs7Data pkcs7Data) throws CertificateException, SignatureException {
        if (pkcs7Data.signers() == null || pkcs7Data.signers().length == 0) {
            throw new SignatureException(SignatureException.UNDEFINED, "No signer information is present");
        }
        X509Certificate[] certificates = pkcs7Data.certificates();
        if (certificates != null) {
            for (X509Certificate certificate : certificates) {
                if (certificate != null) {
                    try {
                        certificate.checkValidity(Date.from(Instant.now()));
                    } catch (java.security.cert.CertificateException exception) {
                        throw new CertificateException(CertificateException.INVALID, exception.getMessage());
                    }
                }
            }
        }
        for (SignerData signer : pkcs7Data.signers()) {
            if (signer.certificate() == null) {
                throw new SignatureException(SignatureException.UNDEFINED,
                        "Signer certificate could not be resolved");
            }
            validateTrustChain(signer.certificate(), certificates);
        }
        try {
            DoJaPkcs7Support.verify((DoJaPkcs7Support.ParsedSignedData) pkcs7Data.pkcs7());
            return true;
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new SignatureException(SignatureException.UNDEFINED, exception.getMessage());
        }
    }

    public static byte[] generateSignedData(byte[] content, String digestAlgorithm, boolean signedDataContent)
            throws SignatureException {
        try {
            HostIdentity identity = hostIdentity();
            return DoJaPkcs7Support.generate(content, digestAlgorithm, signedDataContent,
                    identity.privateKey(), identity.certificateChain());
        } catch (GeneralSecurityException exception) {
            Throwable cause = exception.getCause();
            throw new SignatureException(SignatureException.SIGN_ERROR,
                    cause == null ? "PKCS#7 generation failed" : cause.getMessage());
        }
    }

    public static String distinguishedNameAttribute(String distinguishedName, String attr) {
        if (attr == null) {
            throw new NullPointerException("attr");
        }
        if (distinguishedName == null) {
            return null;
        }
        validateDnAttribute(attr);
        try {
            LdapName ldapName = new LdapName(distinguishedName);
            String requested = attr.toUpperCase(Locale.ROOT);
            String value = null;
            for (Rdn rdn : ldapName.getRdns()) {
                if (requested.equalsIgnoreCase(rdn.getType())) {
                    value = String.valueOf(rdn.getValue());
                }
            }
            return value;
        } catch (InvalidNameException exception) {
            return null;
        }
    }

    public static void validateDnAttribute(String attr) {
        if (attr == null) {
            throw new NullPointerException("attr");
        }
        String normalized = attr.toUpperCase(Locale.ROOT);
        if (!normalized.equals("CN")
                && !normalized.equals("OU")
                && !normalized.equals("O")
                && !normalized.equals("C")) {
            throw new IllegalArgumentException("Unsupported distinguished-name attribute: " + attr);
        }
    }

    public static String hex(BigInteger value) {
        if (value == null) {
            return null;
        }
        return value.toString(16).toUpperCase(Locale.ROOT);
    }

    private static void validateTrustChain(X509Certificate signerCertificate, X509Certificate[] embeddedCertificates)
            throws CertificateException {
        try {
            Set<TrustAnchor> anchors = trustAnchors();
            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(signerCertificate);
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(anchors, selector);
            parameters.setRevocationEnabled(false);
            Collection<X509Certificate> collection = new ArrayList<>();
            if (embeddedCertificates != null) {
                collection.addAll(Arrays.asList(embeddedCertificates));
            }
            collection.add(signerCertificate);
            CertStore certStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(collection));
            parameters.addCertStore(certStore);
            CertPathBuilder.getInstance("PKIX").build(parameters);
        } catch (GeneralSecurityException exception) {
            throw new CertificateException(CertificateException.UNTRUSTED_CA, exception.getMessage());
        }
    }

    private static Set<TrustAnchor> trustAnchors() {
        Set<TrustAnchor> cached = trustAnchors;
        if (cached != null) {
            return cached;
        }
        synchronized (DoJaCryptoSupport.class) {
            if (trustAnchors == null) {
                trustAnchors = loadTrustAnchors();
            }
            return trustAnchors;
        }
    }

    private static Set<TrustAnchor> loadTrustAnchors() {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            Set<TrustAnchor> anchors = new LinkedHashSet<>();
            for (TrustManager trustManager : factory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager x509TrustManager) {
                    for (X509Certificate issuer : x509TrustManager.getAcceptedIssuers()) {
                        anchors.add(new TrustAnchor(issuer, null));
                    }
                }
            }
            X509Certificate[] hostChain = hostIdentity().certificateChain();
            if (hostChain.length > 0 && hostChain[0] != null) {
                anchors.add(new TrustAnchor(hostChain[0], null));
            }
            return anchors;
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to load default trust anchors", exception);
        }
    }

    private static HostIdentity createHostIdentity() {
        try {
            byte[] privateKeyDer = Base64.getDecoder().decode(HOST_PRIVATE_KEY_DER_BASE64);
            byte[] certificateDer = Base64.getDecoder().decode(HOST_CERTIFICATE_DER_BASE64);
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(privateKeyDer));
            X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(certificateDer));
            return new HostIdentity(privateKey, new X509Certificate[]{certificate});
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to create host signing identity", exception);
        }
    }

    public static CipherException translateCipherFinalException(GeneralSecurityException exception) {
        if (exception instanceof BadPaddingException || exception instanceof IllegalBlockSizeException) {
            return new CipherException(CipherException.BAD_PADDING, exception.getMessage());
        }
        return new CipherException(CipherException.UNDEFINED, exception.getMessage());
    }

    public record AlgorithmSpec(int cipherId,
                                String keyAlgorithm,
                                String transformation,
                                int keyLengthBytes,
                                int blockSizeBytes) {
    }

    public record WrappedSessionKey(int sessionCipher, byte[] sessionKey) {
        public WrappedSessionKey {
            sessionKey = sessionKey.clone();
        }

        @Override
        public byte[] sessionKey() {
            return sessionKey.clone();
        }
    }

    public record HostIdentity(PrivateKey privateKey, X509Certificate[] certificateChain) {
        public HostIdentity {
            certificateChain = certificateChain.clone();
        }

        @Override
        public X509Certificate[] certificateChain() {
            return certificateChain.clone();
        }
    }

    public record Pkcs7Data(Object pkcs7,
                            byte[] encoded,
                            byte[] content,
                            X509Certificate[] certificates,
                            SignerData[] signers) {
        public Pkcs7Data {
            encoded = encoded.clone();
            content = DoJaCryptoSupport.clone(content);
            certificates = certificates == null ? null : certificates.clone();
            signers = signers == null ? null : signers.clone();
        }

        @Override
        public byte[] encoded() {
            return encoded.clone();
        }

        @Override
        public byte[] content() {
            return DoJaCryptoSupport.clone(content);
        }

        @Override
        public X509Certificate[] certificates() {
            return certificates == null ? null : certificates.clone();
        }

        @Override
        public SignerData[] signers() {
            return signers == null ? null : signers.clone();
        }
    }

    public record SignerData(Object signer,
                             String issuerDn,
                             String serialNumberHex,
                             X509Certificate certificate) {
    }
}
