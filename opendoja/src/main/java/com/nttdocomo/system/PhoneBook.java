package com.nttdocomo.system;

import com.nttdocomo.device.location.Location;
import com.nttdocomo.lang.XString;

/**
 * Represents one native phone-book entry.
 */
public final class PhoneBook implements MailConstants, PhoneBookConstants {
    private final _SystemSupport.PhoneBookEntry entry;

    PhoneBook(_SystemSupport.PhoneBookEntry entry) {
        this.entry = entry;
    }

    /**
     * Obtains a phone-book entry through native-style user selection.
     *
     * @return the selected entry, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static final PhoneBook selectEntry() throws InterruptedOperationException {
        return _SystemSupport.selectPhoneBook();
    }

    /**
     * Gets a phone-book entry by entry ID.
     *
     * @param id the entry ID
     * @return the requested entry
     * @throws StoreException if the entry does not exist
     */
    public static final PhoneBook getEntry(int id) throws StoreException {
        return _SystemSupport.getPhoneBook(id);
    }

    /**
     * Registers a phone-book entry.
     *
     * @param name the name
     * @param kana the kana reading
     * @param phoneNumbers the phone numbers
     * @param mailAddresses the mail addresses
     * @param groupName the group name, or {@code null}
     * @return the entry ID and group ID, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int[] addEntry(String name, String kana, String[] phoneNumbers, String[] mailAddresses, String groupName)
            throws InterruptedOperationException {
        try {
            return _SystemSupport.addPhoneBook(new PhoneBookParam(name, kana, phoneNumbers, mailAddresses, groupName));
        } catch (StoreException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    /**
     * Registers a phone-book entry.
     *
     * @param name the name
     * @param kana the kana reading
     * @param phoneNumbers the phone numbers
     * @param mailAddresses the mail addresses
     * @param groupId the group ID, or {@code -1}
     * @return the entry ID and group ID, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if the specified group ID does not exist
     */
    public static int[] addEntry(String name, String kana, String[] phoneNumbers, String[] mailAddresses, int groupId)
            throws InterruptedOperationException, StoreException {
        return _SystemSupport.addPhoneBook(new PhoneBookParam(name, kana, phoneNumbers, mailAddresses, groupId));
    }

    /**
     * Registers a phone-book entry.
     *
     * @param param the phone-book parameter object
     * @return the entry ID and group ID, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if the specified group ID does not exist
     */
    public static int[] addEntry(PhoneBookParam param) throws InterruptedOperationException, StoreException {
        return _SystemSupport.addPhoneBook(param);
    }

    /**
     * Gets the entry ID.
     *
     * @return the entry ID
     */
    public final int getId() {
        return entry.id;
    }

    /**
     * Gets the full name.
     *
     * @return the full name as an {@link XString}
     */
    public final XString getName() {
        return new XString(_SystemSupport.combineName(entry.singleName,
                entry.nameParts[FAMILY_NAME], entry.nameParts[GIVEN_NAME]));
    }

    /**
     * Gets one name part.
     *
     * @param part the name part to obtain
     * @return the name part as an {@link XString}, or {@code null}
     */
    public XString getName(int part) {
        _SystemSupport.validatePhoneBookPart(part);
        if (entry.singleName != null) {
            return part == FAMILY_NAME ? new XString(entry.singleName) : null;
        }
        return new XString(entry.nameParts[part] == null ? "" : entry.nameParts[part]);
    }

    /**
     * Gets the full kana reading.
     *
     * @return the kana reading as an {@link XString}
     */
    public XString getKana() {
        return new XString(_SystemSupport.combineName(entry.singleKana,
                entry.kanaParts[FAMILY_NAME], entry.kanaParts[GIVEN_NAME]));
    }

    /**
     * Gets one kana-reading part.
     *
     * @param part the kana-reading part to obtain
     * @return the kana-reading part as an {@link XString}, or {@code null}
     */
    public XString getKana(int part) {
        _SystemSupport.validatePhoneBookPart(part);
        if (entry.singleKana != null) {
            return part == FAMILY_NAME ? new XString(entry.singleKana) : null;
        }
        return new XString(entry.kanaParts[part] == null ? "" : entry.kanaParts[part]);
    }

    /**
     * Gets the phone numbers.
     *
     * @return the phone numbers as {@link XString} objects, or {@code null}
     */
    public XString[] getPhoneNumbers() {
        return _SystemSupport.xAddressParts(entry.phoneNumbers, ADDRESS_FULL, true);
    }

    /**
     * Gets one phone number.
     *
     * @param index the phone-number index
     * @return the phone number
     */
    public XString getPhoneNumber(int index) {
        return getPhoneNumbers()[index];
    }

    /**
     * Gets the mail addresses.
     *
     * @param part the address part to obtain
     * @return the mail addresses as {@link XString} objects, or {@code null}
     */
    public XString[] getMailAddresses(int part) {
        return _SystemSupport.xAddressParts(entry.mailAddresses, part, true);
    }

    /**
     * Gets one mail address.
     *
     * @param index the mail-address index
     * @param part the address part to obtain
     * @return the mail address
     */
    public XString getMailAddress(int index, int part) {
        return getMailAddresses(part)[index];
    }

    /**
     * Gets the group name.
     *
     * @return the group name as an {@link XString}
     */
    public XString getGroupName() {
        if (entry.groupId < 0) {
            return new XString("");
        }
        try {
            return PhoneBookGroup.getEntry(entry.groupId).getName();
        } catch (StoreException exception) {
            return new XString("");
        }
    }

    /**
     * Gets the group ID.
     *
     * @return the group ID, or {@code -1}
     */
    public int getGroupId() {
        return entry.groupId;
    }

    /**
     * Gets the stored location information.
     *
     * @return a copy of the location information, or {@code null}
     */
    public Location getLocation() {
        return _SystemSupport.copyLocation(entry.location);
    }
}
