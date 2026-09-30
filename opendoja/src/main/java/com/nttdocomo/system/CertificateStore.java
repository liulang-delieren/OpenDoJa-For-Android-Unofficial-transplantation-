package com.nttdocomo.system;

/**
 * Provides access to native user-certificate selection.
 */
public final class CertificateStore {
    /** Indicates the UIM certificate entry ID (=0). */
    public static final int CERTIFICATE_UIM_ID = _SystemSupport.CERTIFICATE_UIM_ID;

    private CertificateStore() {
    }

    /**
     * Obtains a user-certificate entry ID through native-style user selection.
     *
     * @return the selected certificate entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if certificate entry selection fails
     */
    public static int selectEntryId() throws InterruptedOperationException, StoreException {
        return _SystemSupport.selectCertificateEntryId();
    }
}
