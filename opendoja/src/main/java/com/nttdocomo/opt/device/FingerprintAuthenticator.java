package com.nttdocomo.opt.device;

import com.nttdocomo.system.InterruptedOperationException;
import com.nttdocomo.system.StoreException;

/**
 * Provides the means to access the handset's native fingerprint
 * authentication function.
 * Depending on the terminal, this function may not be supported.
 * If it is unsupported, {@link UnsupportedOperationException} is raised when a
 * method is called.
 */
public class FingerprintAuthenticator {
    /**
     * Applications cannot create instances of this class directly.
     */
    protected FingerprintAuthenticator() {
    }

    /**
     * Gets the fingerprint-authentication object.
     * The same reference is always returned.
     *
     * @return the fingerprint-authentication object
     */
    public static FingerprintAuthenticator getFingerprintAuthenticator() {
        return _OptionalDeviceSupport.fingerprintAuthenticator();
    }

    /**
     * Obtains a fingerprint-data entry ID through user interaction.
     *
     * @return the selected fingerprint-data entry ID, or {@code -1}
     * @throws InterruptedOperationException never thrown by this host
     */
    public int select() throws InterruptedOperationException {
        return _OptionalDeviceSupport.fingerprintSelect();
    }

    /**
     * Authenticates against all fingerprint data registered in the device.
     *
     * @return the authenticated fingerprint-data entry ID, or {@code -1}
     * @throws InterruptedOperationException never thrown by this host
     */
    public int authenticate() throws InterruptedOperationException {
        return _OptionalDeviceSupport.fingerprintAuthenticateAll();
    }

    /**
     * Authenticates against the specified fingerprint entry.
     *
     * @param id the entry ID to authenticate against
     * @return {@code true} if the specified entry matched, otherwise
     *         {@code false}
     * @throws StoreException if the specified entry ID is not present
     * @throws InterruptedOperationException never thrown by this host
     */
    public boolean authenticate(int id) throws StoreException, InterruptedOperationException {
        return _OptionalDeviceSupport.fingerprintAuthenticateOne(id);
    }

    /**
     * Authenticates against one of the specified fingerprint entries.
     *
     * @param id the entry IDs to authenticate against
     * @return the matched fingerprint-data entry ID, or {@code -1}
     * @throws StoreException if none of the specified IDs are present
     * @throws InterruptedOperationException never thrown by this host
     */
    public int authenticate(int[] id) throws StoreException, InterruptedOperationException {
        return _OptionalDeviceSupport.fingerprintAuthenticateMany(id);
    }
}
