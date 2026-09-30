package com.nttdocomo.system;

/**
 * Represents one native Toruca-data entry.
 */
public final class TorucaStore {
    private final int id;
    private final Toruca toruca;

    TorucaStore(int id, Toruca toruca) {
        this.id = id;
        this.toruca = toruca;
    }

    /**
     * Obtains a Toruca-data entry through native-style user selection.
     *
     * @return the selected entry, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static TorucaStore selectEntry() throws InterruptedOperationException {
        return _SystemSupport.selectToruca();
    }

    /**
     * Gets a Toruca-data entry by entry ID.
     *
     * @param id the entry ID
     * @return the requested entry
     * @throws StoreException if the entry does not exist
     */
    public static TorucaStore getEntry(int id) throws StoreException {
        return _SystemSupport.getToruca(id);
    }

    /**
     * Searches Toruca-card entries by host and IP-ID prefix.
     *
     * @param host the URL FQDN to match
     * @param ipid the IP-ID to match
     * @return matching entry IDs, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int[] findByHostAndIpid(String host, String ipid) throws InterruptedOperationException {
        return _SystemSupport.findTorucaByHostAndIpid(host, ipid);
    }

    /**
     * Registers Toruca data.
     *
     * @param toruca the Toruca data to register
     * @return the registered entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(Toruca toruca) throws InterruptedOperationException {
        return _SystemSupport.addToruca(toruca);
    }

    /**
     * Gets the entry ID.
     *
     * @return the entry ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the Toruca data.
     *
     * @return the Toruca data
     */
    public Toruca getToruca() {
        return toruca;
    }

    /**
     * Gets the remaining bytes that can be set in the specified Toruca data.
     *
     * @param toruca the Toruca data
     * @return the remaining bytes
     */
    public static int getRemainingBytes(Toruca toruca) {
        return _SystemSupport.torucaRemainingBytes(toruca);
    }
}
