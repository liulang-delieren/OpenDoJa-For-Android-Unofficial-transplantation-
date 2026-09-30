package com.nttdocomo.system;

/**
 * Defines selected Owner Profile items.
 */
public final class OwnerProfile {
    /** Indicates the full name item (=1). */
    public static final int NAME = 1;
    /** Indicates the family-name item (=2). */
    public static final int FAMILY_NAME = 2;
    /** Indicates the given-name item (=3). */
    public static final int GIVEN_NAME = 3;
    /** Indicates the full-kana item (=4). */
    public static final int KANA = 4;
    /** Indicates the family-name-kana item (=5). */
    public static final int FAMILY_NAME_KANA = 5;
    /** Indicates the given-name-kana item (=6). */
    public static final int GIVEN_NAME_KANA = 6;
    /** Indicates the first telephone-number item (=7). */
    public static final int TELEPHONE_NUMBER_1 = 7;
    /** Indicates the second telephone-number item (=8). */
    public static final int TELEPHONE_NUMBER_2 = 8;
    /** Indicates the first email-address item (=9). */
    public static final int EMAIL_ADDRESS_1 = 9;
    /** Indicates the second email-address item (=10). */
    public static final int EMAIL_ADDRESS_2 = 10;
    /** Indicates the postal-code item (=11). */
    public static final int POSTAL_CODE = 11;
    /** Indicates the full-address item (=12). */
    public static final int ADDRESS = 12;
    /** Indicates the address-region item (=13). */
    public static final int ADDRESS_REGION = 13;
    /** Indicates the address-locality item (=14). */
    public static final int ADDRESS_LOCALITY = 14;
    /** Indicates the address-street item (=15). */
    public static final int ADDRESS_STREET = 15;
    /** Indicates the address-extended item (=16). */
    public static final int ADDRESS_EXTENDED = 16;
    /** Indicates the full birth-date item (=17). */
    public static final int BIRTH_DATE = 17;
    /** Indicates the birth-year item (=18). */
    public static final int BIRTH_DATE_YEAR = 18;
    /** Indicates the birth-month item (=19). */
    public static final int BIRTH_DATE_MONTH = 19;
    /** Indicates the birth-day item (=20). */
    public static final int BIRTH_DATE_DAY = 20;

    private final int[] selectedItems;

    OwnerProfile(int[] selectedItems) {
        this.selectedItems = selectedItems == null ? null : selectedItems.clone();
    }

    /**
     * Gets selected Owner Profile data.
     *
     * @return the selected Owner Profile data, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static OwnerProfile getProfileData() throws InterruptedOperationException {
        return _SystemSupport.getOwnerProfile(null);
    }

    /**
     * Gets selected Owner Profile data.
     *
     * @param items the initially selected items, or {@code null}
     * @return the selected Owner Profile data, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static OwnerProfile getProfileData(int[] items) throws InterruptedOperationException {
        return _SystemSupport.getOwnerProfile(items);
    }

    /**
     * Gets the selected item list.
     *
     * @return a copy of the selected item list, or {@code null}
     */
    public int[] getSelectedItems() {
        return selectedItems == null ? null : selectedItems.clone();
    }

    /**
     * Gets the data corresponding to the specified item.
     *
     * @param item the item to obtain
     * @return the string value, {@code ""}, {@code "0000"}, {@code "00"}, or {@code null}
     */
    public String getData(int item) {
        if (selectedItems == null) {
            return null;
        }
        boolean selected = false;
        for (int selectedItem : selectedItems) {
            if (selectedItem == item) {
                selected = true;
                break;
            }
        }
        return selected ? _SystemSupport.ownerProfileData(item) : null;
    }
}
