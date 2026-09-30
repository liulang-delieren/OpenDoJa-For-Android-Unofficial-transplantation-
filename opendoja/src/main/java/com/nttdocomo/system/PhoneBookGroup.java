package com.nttdocomo.system;

import com.nttdocomo.lang.XString;

/**
 * Represents one native phone-book-group entry.
 */
public final class PhoneBookGroup {
    private final int id;
    private final String name;

    PhoneBookGroup(_SystemSupport.PhoneBookGroupEntry entry) {
        this.id = entry.id;
        this.name = entry.name == null ? "" : entry.name;
    }

    /**
     * Obtains a phone-book-group entry through native-style user selection.
     *
     * @return the selected entry, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static PhoneBookGroup selectEntry() throws InterruptedOperationException {
        return _SystemSupport.selectPhoneBookGroup();
    }

    /**
     * Gets a phone-book-group entry by entry ID.
     *
     * @param id the entry ID
     * @return the requested entry
     * @throws StoreException if the entry does not exist
     */
    public static PhoneBookGroup getEntry(int id) throws StoreException {
        return _SystemSupport.getPhoneBookGroup(id);
    }

    /**
     * Registers a phone-book group.
     *
     * @param name the group name
     * @return the registered entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(String name) throws InterruptedOperationException {
        return _SystemSupport.addPhoneBookGroup(name);
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
     * Gets the group name.
     *
     * @return the group name as an {@link XString}
     */
    public XString getName() {
        return new XString(name);
    }
}
