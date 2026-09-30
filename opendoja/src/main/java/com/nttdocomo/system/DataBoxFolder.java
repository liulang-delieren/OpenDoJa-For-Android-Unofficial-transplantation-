package com.nttdocomo.system;

/**
 * Provides access to native Data Box folders.
 */
public final class DataBoxFolder {
    /** Indicates the My Picture folder type (=1). */
    public static final int FOLDER_MY_PICTURE = 1;

    /** Indicates the i-motion folder type (=2). */
    public static final int FOLDER_I_MOTION = 2;

    private DataBoxFolder() {
    }

    /**
     * Creates a folder directly under the specified Data Box root.
     *
     * @param folder the Data Box root kind
     * @param name the folder name
     * @return the created folder entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(int folder, String name) throws InterruptedOperationException {
        return _SystemSupport.addDataBoxFolder(folder, name);
    }

    /**
     * Obtains a folder entry ID under the specified Data Box root.
     *
     * @param folder the Data Box root kind
     * @return the selected folder entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int selectEntryId(int folder) throws InterruptedOperationException {
        return _SystemSupport.selectDataBoxFolder(folder);
    }
}
