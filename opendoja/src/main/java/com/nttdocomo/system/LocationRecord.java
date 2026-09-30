package com.nttdocomo.system;

import com.nttdocomo.device.location.Location;

/**
 * Represents one native location-history entry.
 */
public final class LocationRecord {
    final int id;
    final Location location;

    LocationRecord(int id, Location location) {
        this.id = id;
        this.location = _SystemSupport.copyLocation(location);
    }

    /**
     * Obtains a location-history entry through native-style user selection.
     *
     * @return the selected entry, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static LocationRecord selectEntry() throws InterruptedOperationException {
        return _SystemSupport.selectLocationRecord();
    }

    /**
     * Gets a location-history entry by entry ID.
     *
     * @param id the entry ID
     * @return the requested entry
     * @throws StoreException if the entry does not exist
     */
    public static LocationRecord getEntry(int id) throws StoreException {
        return _SystemSupport.getLocationRecord(id);
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
     * Gets the stored location result.
     *
     * @return a copy of the stored location result
     */
    public Location getLocation() {
        return _SystemSupport.copyLocation(location);
    }
}
