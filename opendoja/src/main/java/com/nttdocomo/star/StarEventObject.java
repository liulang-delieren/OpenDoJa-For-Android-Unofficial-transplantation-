package com.nttdocomo.star;

/**
 * Minimal Star event payload used by compatibility callbacks.
 */
public class StarEventObject {
    public static final int STAR_CALLED_BY_DTV = 1;
    public static final int STAR_FELICA_ADHOC_REQUEST_RECEIVED = 2;
    public static final int STAR_FELICA_PUSHED = 3;
    public static final int STAR_STATECHANGE_CLAM_CLOSE = 4;
    public static final int STAR_STATECHANGE_CLAM_OPEN = 5;

    private final int type;

    public StarEventObject(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
