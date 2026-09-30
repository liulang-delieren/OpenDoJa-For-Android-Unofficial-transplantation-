package com.nttdocomo.star;

/**
 * Star event indicating the application was invoked from DTV.
 */
public final class CalledByDTVEvent extends StarEventObject {
    public CalledByDTVEvent() {
        super(STAR_CALLED_BY_DTV);
    }
}
