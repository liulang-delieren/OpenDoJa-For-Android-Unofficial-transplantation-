package com.nttdocomo.star.system;

import opendoja.host.OpenDoJaLog;

import java.util.Hashtable;

/**
 * Minimal Star contents API bridge for titles that request handset-managed
 * menu actions such as MyMenu integration.
 */
public final class Contents {
    private Contents() {
    }

    public static void sendMyMenuRequest(Hashtable parameters, boolean showDialog)
            throws ContentsException, com.nttdocomo.system.InterruptedOperationException {
        OpenDoJaLog.info(Contents.class, () ->
                "Simulated Star Contents.sendMyMenuRequest params=" + parameters + " showDialog=" + showDialog);
    }
}
