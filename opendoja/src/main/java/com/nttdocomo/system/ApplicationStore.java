package com.nttdocomo.system;

/**
 * Represents an i-appli data entry available from launcher mode.
 */
public final class ApplicationStore {
    private final int id;

    ApplicationStore(int id) {
        this.id = id;
    }

    /**
     * Obtains an i-appli-data entry through native-style user selection.
     *
     * @return the selected entry, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static ApplicationStore selectEntry() throws InterruptedOperationException {
        return _SystemSupport.selectApplication();
    }

    /**
     * Gets the i-appli-data entry ID.
     *
     * @return the entry ID
     */
    public int getId() {
        return id;
    }
}
