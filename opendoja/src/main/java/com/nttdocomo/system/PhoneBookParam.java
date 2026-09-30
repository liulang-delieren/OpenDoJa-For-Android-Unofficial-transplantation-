package com.nttdocomo.system;

import com.nttdocomo.device.location.Location;

import java.util.Arrays;

/**
 * Defines parameters for registering phone-book data.
 */
public final class PhoneBookParam implements PhoneBookConstants {
    String singleName;
    final String[] nameParts = new String[2];
    String singleKana;
    final String[] kanaParts = new String[2];
    String[] phoneNumbers;
    String[] mailAddresses;
    int groupId = -1;
    String groupName;
    Location location;

    private PhoneBookParam(String name, String kana, String[] phoneNumbers, String[] mailAddresses, int groupId, String groupName) {
        if (name != null) {
            setName(name);
        }
        if (kana != null) {
            setKana(kana);
        }
        setPhoneNumbers(phoneNumbers);
        setMailAddresses(mailAddresses);
        if (groupName != null) {
            setGroupName(groupName);
        } else {
            setGroupId(groupId);
        }
    }

    /**
     * Creates a phone-book parameter object for new registration.
     * This is equivalent to {@code PhoneBookParam(null, null, null, null, -1)}.
     */
    public PhoneBookParam() {
        this(null, null, null, null, -1, null);
    }

    /**
     * Creates a phone-book parameter object for new registration.
     *
     * @param name the name
     * @param kana the kana reading
     * @param phoneNumbers the phone numbers
     * @param mailAddresses the mail addresses
     * @param groupName the group name, or {@code null}
     */
    public PhoneBookParam(String name, String kana, String[] phoneNumbers, String[] mailAddresses, String groupName) {
        this(name, kana, phoneNumbers, mailAddresses, -1, groupName);
    }

    /**
     * Creates a phone-book parameter object for new registration.
     *
     * @param name the name
     * @param kana the kana reading
     * @param phoneNumbers the phone numbers
     * @param mailAddresses the mail addresses
     * @param groupId the group ID, or {@code -1}
     */
    public PhoneBookParam(String name, String kana, String[] phoneNumbers, String[] mailAddresses, int groupId) {
        this(name, kana, phoneNumbers, mailAddresses, groupId, null);
    }

    /**
     * Gets the combined name.
     *
     * @return the name, or {@code null}
     */
    public String getName() {
        return singleName;
    }

    /**
     * Sets the combined name.
     *
     * @param name the name, or {@code null} to clear it
     */
    public void setName(String name) {
        singleName = name;
        nameParts[FAMILY_NAME] = null;
        nameParts[GIVEN_NAME] = null;
    }

    /**
     * Gets one separated name part.
     *
     * @param part the name part to obtain
     * @return the name part, or {@code null}
     */
    public String getName(int part) {
        _SystemSupport.validatePhoneBookPart(part);
        return nameParts[part];
    }

    /**
     * Sets one separated name part.
     *
     * @param part the name part to set
     * @param name the name part, or {@code null} to clear it
     */
    public void setName(int part, String name) {
        _SystemSupport.validatePhoneBookPart(part);
        singleName = null;
        nameParts[part] = name;
    }

    /**
     * Gets the combined kana reading.
     *
     * @return the kana reading, or {@code null}
     */
    public String getKana() {
        return singleKana;
    }

    /**
     * Sets the combined kana reading.
     *
     * @param kana the kana reading, or {@code null} to clear it
     */
    public void setKana(String kana) {
        singleKana = kana;
        kanaParts[FAMILY_NAME] = null;
        kanaParts[GIVEN_NAME] = null;
    }

    /**
     * Gets one separated kana-reading part.
     *
     * @param part the part to obtain
     * @return the kana-reading part, or {@code null}
     */
    public String getKana(int part) {
        _SystemSupport.validatePhoneBookPart(part);
        return kanaParts[part];
    }

    /**
     * Sets one separated kana-reading part.
     *
     * @param part the part to set
     * @param name the kana-reading part, or {@code null} to clear it
     */
    public void setKana(int part, String name) {
        _SystemSupport.validatePhoneBookPart(part);
        singleKana = null;
        kanaParts[part] = name;
    }

    /**
     * Gets the phone numbers.
     *
     * @return a copy of the phone numbers, or {@code null}
     */
    public String[] getPhoneNumbers() {
        return _SystemSupport.copyStrings(phoneNumbers);
    }

    /**
     * Gets one phone number.
     *
     * @param index the phone-number index
     * @return the phone number
     */
    public String getPhoneNumber(int index) {
        return getPhoneNumbers()[index];
    }

    /**
     * Sets the phone numbers.
     *
     * @param phoneNumbers the phone numbers, or {@code null} to clear them
     */
    public void setPhoneNumbers(String[] phoneNumbers) {
        _SystemSupport.validatePhoneNumbers(phoneNumbers);
        this.phoneNumbers = phoneNumbers == null || phoneNumbers.length == 0 ? null : phoneNumbers.clone();
    }

    /**
     * Adds one phone number.
     *
     * @param phoneNumber the phone number to add
     */
    public void addPhoneNumber(String phoneNumber) {
        java.util.Objects.requireNonNull(phoneNumber, "phoneNumber");
        _SystemSupport.validatePhoneNumber(phoneNumber);
        int current = phoneNumbers == null ? 0 : phoneNumbers.length;
        if (current >= _SystemSupport.PHONE_BOOK_ITEM_MAX) {
            throw new IllegalStateException("Too many phone numbers");
        }
        String[] updated = Arrays.copyOf(phoneNumbers == null ? new String[0] : phoneNumbers, current + 1);
        updated[current] = phoneNumber;
        phoneNumbers = updated;
    }

    /**
     * Gets the mail addresses.
     *
     * @return a copy of the mail addresses, or {@code null}
     */
    public String[] getMailAddresses() {
        return _SystemSupport.copyStrings(mailAddresses);
    }

    /**
     * Gets one mail address.
     *
     * @param index the mail-address index
     * @return the mail address
     */
    public String getMailAddress(int index) {
        return getMailAddresses()[index];
    }

    /**
     * Sets the mail addresses.
     *
     * @param mailAddresses the mail addresses, or {@code null} to clear them
     */
    public void setMailAddresses(String[] mailAddresses) {
        _SystemSupport.validateMailAddressesForPhoneBook(mailAddresses);
        this.mailAddresses = mailAddresses == null || mailAddresses.length == 0 ? null : mailAddresses.clone();
    }

    /**
     * Adds one mail address.
     *
     * @param mailAddress the mail address to add
     */
    public void addMailAddress(String mailAddress) {
        java.util.Objects.requireNonNull(mailAddress, "mailAddress");
        _SystemSupport.validateMailAddress(mailAddress);
        int current = mailAddresses == null ? 0 : mailAddresses.length;
        if (current >= _SystemSupport.PHONE_BOOK_ITEM_MAX) {
            throw new IllegalStateException("Too many mail addresses");
        }
        String[] updated = Arrays.copyOf(mailAddresses == null ? new String[0] : mailAddresses, current + 1);
        updated[current] = mailAddress;
        mailAddresses = updated;
    }

    /**
     * Gets the group ID.
     *
     * @return the group ID, or {@code -1}
     */
    public int getGroupId() {
        return groupName == null ? groupId : -1;
    }

    /**
     * Sets the group ID.
     *
     * @param id the group ID
     */
    public void setGroupId(int id) {
        groupId = id;
        groupName = null;
    }

    /**
     * Gets the group name.
     *
     * @return the group name, or {@code null}
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the group name.
     *
     * @param name the group name, or {@code null} to clear it
     */
    public void setGroupName(String name) {
        groupName = name;
        groupId = -1;
    }

    /**
     * Sets the location information.
     *
     * @param location the location information, or {@code null} to clear it
     */
    public void setLocation(Location location) {
        this.location = _SystemSupport.copyLocation(location);
    }

    /**
     * Gets the location information.
     *
     * @return a copy of the location information, or {@code null}
     */
    public Location getLocation() {
        return _SystemSupport.copyLocation(location);
    }
}
