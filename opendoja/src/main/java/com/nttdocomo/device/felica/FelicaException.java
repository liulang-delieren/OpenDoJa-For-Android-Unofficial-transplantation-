package com.nttdocomo.device.felica;

/**
 * Represents an exception related to the FeliCa function.
 */
public final class FelicaException extends Exception {
    /** Represents an error kind meaning an undefined failure (=0x00). */
    public static final int ID_UNDEFINED_ERROR = 0;
    /** Represents an error kind meaning open failure (=0x01). */
    public static final int ID_OPEN_ERROR = 1;
    /** Represents an error kind meaning close failure (=0x02). */
    public static final int ID_CLOSE_ERROR = 2;
    /** Represents an error kind meaning polling failure (=0x03). */
    public static final int ID_POLLING_ERROR = 3;
    /** Represents an error kind meaning read failure (=0x04). */
    public static final int ID_READ_ERROR = 4;
    /** Represents an error kind meaning write failure (=0x05). */
    public static final int ID_WRITE_ERROR = 5;
    /** Represents an error kind meaning activate failure (=0x06). */
    public static final int ID_ACTIVATE_ERROR = 6;
    /** Represents an error kind meaning PIN verification, change, or enable/disable switching failure (=0x07). */
    public static final int ID_EXECUTEPIN_ERROR = 7;
    /** Represents an error kind meaning key-version retrieval failure (=0x08). */
    public static final int ID_GETKEYVERSION_ERROR = 8;
    /** Represents an error kind meaning inactivate failure (=0x09). */
    public static final int ID_INACTIVATE_ERROR = 9;
    /** Represents an error kind meaning container issue-information retrieval failure (=0x0a). */
    public static final int ID_GETISSUEINFO_ERROR = 10;
    /** Represents an error kind meaning command-packet attribute specification failure (=0x0b). */
    public static final int ID_SETPARAMETER_ERROR = 11;
    /** Represents an error kind meaning RF-power stop failure (=0x0c). */
    public static final int ID_TURNOFF_RFPOWER_ERROR = 12;
    /** Represents an error kind meaning wireless communication-speed setting failure (=0x0d). */
    public static final int ID_NEGOTIATE_BAUDRATE_ERROR = 13;
    /** Represents an error kind meaning PIN-attribute reference failure (=0x0e). */
    public static final int ID_CHECKPIN_ERROR = 14;
    /** Represents an error kind meaning ad-hoc mode retrieval failure (=0x0f). */
    public static final int ID_GETADHOCSTATE_ERROR = 15;
    /** Represents an error kind meaning reset failure (=0x10). */
    public static final int ID_RESET_ERROR = 16;
    /** Represents an error kind meaning locked-node-list retrieval failure while a remote individual area is stopped (=0x11). */
    public static final int ID_LOCKED_NODELIST_ERROR = 17;

    /** Represents an error content meaning an undefined error (=0x00). */
    public static final int TYPE_UNDEFINED_ERROR = 0;
    /** Represents an error content meaning a purse-value shortage for decrement, or purse-value overflow for cashback (=0x01). */
    public static final int TYPE_PURSE_ERROR = 1;
    /** Represents an error content meaning a cashback error (=0x02). */
    public static final int TYPE_CASHBACK_ERROR = 2;
    /** Represents an error content meaning an invalid block number specification (=0x03). */
    public static final int TYPE_BLOCK_NO_ERROR = 3;
    /** Represents an error content meaning an invalid simultaneous cyclic-write specification (=0x04). */
    public static final int TYPE_CYCLIC_ERROR = 4;
    /** Represents an error content meaning PIN verification is required (=0x05). */
    public static final int TYPE_PIN_REQUIRED_ERROR = 5;
    /** Represents an error content meaning an illegal PIN-attribute change access (=0x06). */
    public static final int TYPE_SETATTRIBUTE_ERROR = 6;
    /** Represents an error content meaning a non-existent service was specified (=0x07). */
    public static final int TYPE_SERVICE_CODE_ERROR = 7;
    /** Represents an error content meaning an incorrect PIN specification (=0x08). */
    public static final int TYPE_SETPIN_ERROR = 8;
    /** Represents an error content meaning the response-data format is invalid (=0x09). */
    public static final int TYPE_FORMAT_ERROR = 9;
    /** Represents an error content meaning a response timeout (=0x0a). */
    public static final int TYPE_TIMEOUT_ERROR = 10;
    /** Represents an error content meaning FreeArea data-read failure (=0x0b). */
    public static final int TYPE_FREEAREA_READ_ERROR = 11;
    /** Represents an error content meaning FreeArea data-write failure (=0x0c). */
    public static final int TYPE_FREEAREA_WRITE_ERROR = 12;
    /** Represents an error content meaning the simultaneous PIN-unlock limit for the target card was exceeded (=0x0d). */
    public static final int TYPE_PIN_COUNT_OVER_ERROR = 13;
    /** Represents an error content meaning the simultaneous block-processing limit for the target card was exceeded (=0x0e). */
    public static final int TYPE_BLOCK_COUNT_OVER_ERROR = 14;
    /** Represents an error content meaning the PIN verification retry limit was exceeded (=0x0f). */
    public static final int TYPE_PIN_LOCK_OUT_ERROR = 15;
    /** Represents an error content meaning the execution procedure is invalid (=0x10). */
    public static final int TYPE_ILLEGAL_STATE_ERROR = 16;
    /** Represents an error content meaning a device error (=0x11). */
    public static final int TYPE_DEVICE_ERROR = 17;
    /** Represents an error content meaning an unexpected error (=0x12). */
    public static final int TYPE_UNEXPECTED_ERROR = 18;
    /** Represents an error content meaning an external-card error (=0x13). */
    public static final int TYPE_EXTERNAL_CARD_ERROR = 19;
    /** Represents an error content meaning FreeArea polling failed (=0x14). */
    public static final int TYPE_FREEAREA_POLLING_ERROR = 20;
    /** Represents an error content meaning the IDm values do not match (=0x15). */
    public static final int TYPE_IDM_MISMATCH_ERROR = 21;
    /** Represents an error content meaning FreeArea reset failure (=0x16). */
    public static final int TYPE_FREEAREA_RESET_ERROR = 22;
    /** Represents an error content meaning the card is in self mode (=0x17). */
    public static final int TYPE_SELF_MODE = 23;

    private final int id;
    private final int type;
    private final FelicaStatus status;

    FelicaException() {
        this(ID_UNDEFINED_ERROR, TYPE_UNDEFINED_ERROR, null, new FelicaStatus((byte) 0, (byte) 0));
    }

    FelicaException(int id, int type, String message, FelicaStatus status) {
        super(message);
        this.id = id;
        this.type = type;
        this.status = status == null ? new FelicaStatus((byte) 0, (byte) 0) : status;
    }

    /**
     * Returns the error kind.
     *
     * @return the error kind
     */
    public int getID() {
        return id;
    }

    /**
     * Returns the error content.
     *
     * @return the error content
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the error information returned in a response command from an external card.
     *
     * @return the FeliCa status
     */
    public FelicaStatus getFelicaStatus() {
        return status;
    }
}
