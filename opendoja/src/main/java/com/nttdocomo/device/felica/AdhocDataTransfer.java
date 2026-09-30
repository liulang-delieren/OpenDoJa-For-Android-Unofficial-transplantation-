package com.nttdocomo.device.felica;

import com.nttdocomo.io.ConnectionException;
import opendoja.host.DoJaEncoding;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represents ad-hoc data transfer using FeliCa.
 */
public final class AdhocDataTransfer {
    private static final int ADHOC_MODE_0 = 0;
    private static final int ADHOC_MODE_1 = 1;

    private String type;
    private String name;
    private Hashtable options;
    private int state;
    private byte[] idm;

    AdhocDataTransfer() {
    }

    /**
     * Establishes a link with the continuous-data-transfer destination by
     * ad-hoc communication.
     * When the link is established, the IDm of the linked card becomes
     * available.
     *
     * @param type the ADF URL of the transfer-destination application
     * @param name the startup command notified to the transfer-destination
     *             application
     * @param options a hashtable containing startup parameters notified to the
     *                transfer-destination application, or {@code null} if no
     *                startup parameters are needed
     * @throws IOException if the link cannot be established or another I/O
     *         error occurs
     */
    public void setup(String type, String name, Hashtable options) throws IOException {
        FelicaSupport.requireOfflineAccess();
        validateSetupArguments(type, name, options);
        if (state == ADHOC_MODE_1) {
            throw new ConnectionException(ConnectionException.BUSY_RESOURCE, "Ad-hoc link is already established");
        }
        this.type = type;
        this.name = name;
        this.options = options == null ? null : (Hashtable) options.clone();
        this.state = ADHOC_MODE_1;
        this.idm = FelicaSupport.idmFor(OfflineFelica.CARD_EXTERNAL, 0);
    }

    /**
     * Ends continuous data transfer by ad-hoc communication and disconnects
     * the link.
     * If the link is already disconnected, nothing happens.
     * Calling this method discards the IDm held by this object.
     *
     * @throws IOException if I/O fails
     */
    public void terminateAdhoc() throws IOException {
        FelicaSupport.requireOfflineAccess();
        this.state = ADHOC_MODE_0;
        this.idm = null;
    }

    /**
     * Gets the current state (ad-hoc mode) of the ad-hoc communication
     * function.
     *
     * @return the ad-hoc mode
     * @throws FelicaException if ad-hoc state acquisition fails
     * @throws IOException if I/O fails
     */
    public int getAdhocState() throws FelicaException, IOException {
        FelicaSupport.requireOfflineAccess();
        return state;
    }

    /**
     * Returns the IDm held by this object as the access destination.
     * A valid value is returned only while the ad-hoc communication link is
     * established. If the link is not established, {@code null} is returned.
     *
     * @return the IDm (8 bytes), or {@code null} if no link is established
     */
    public byte[] getIDm() {
        return idm == null ? null : idm.clone();
    }

    private static void validateSetupArguments(String adfUrl, String command, Hashtable params) {
        if (adfUrl == null) {
            throw new NullPointerException("type");
        }
        if (adfUrl.getBytes(DoJaEncoding.DEFAULT_CHARSET).length > 255) {
            throw new IllegalArgumentException("type");
        }
        if (command == null) {
            throw new NullPointerException("name");
        }
        if (command.isEmpty() || command.length() > 255) {
            throw new IllegalArgumentException("name");
        }
        validateAsciiGraphic(command, true, "name");
        if (params == null) {
            return;
        }
        int bytes = 0;
        for (Object entryObject : params.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
            if (!(entry.getKey() instanceof String key) || !(entry.getValue() instanceof String value)) {
                throw new ClassCastException();
            }
            validateAsciiGraphic(key, false, "options");
            validateAsciiGraphic(value, false, "options");
            bytes += key.length();
            bytes += value.length();
        }
        if (bytes > 255) {
            throw new IllegalArgumentException("options");
        }
    }

    private static void validateAsciiGraphic(String value, boolean allowSpace, String label) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            boolean ok = allowSpace ? ch >= 0x20 && ch <= 0x7E : ch >= 0x21 && ch <= 0x7E;
            if (!ok) {
                throw new IllegalArgumentException(label);
            }
        }
    }
}
