package com.nttdocomo.star;

/**
 * Star event indicating a FeliCa ad-hoc request.
 */
public final class FelicaAdhocEvent extends StarEventObject {
    public FelicaAdhocEvent() {
        super(STAR_FELICA_ADHOC_REQUEST_RECEIVED);
    }
}
