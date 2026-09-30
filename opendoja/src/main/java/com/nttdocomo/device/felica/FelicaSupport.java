package com.nttdocomo.device.felica;

import com.nttdocomo.device.DeviceException;
import com.nttdocomo.lang.UnsupportedOperationException;
import opendoja.host.DoJaApiUnimplemented;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

final class FelicaSupport {
    static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "opendoja-felica");
        thread.setDaemon(true);
        return thread;
    });
    static final OnlineFelica ONLINE = new OnlineFelica();
    static final FreeArea FREE_AREA = new FreeArea();
    static final AdhocDataTransfer ADHOC = new AdhocDataTransfer();
    static final Map<String, OfflineFelica> OFFLINE = new HashMap<>();
    static boolean open;
    static FelicaPushListener pushListener;
    static boolean onlineProcessing;

    private FelicaSupport() {
    }

    static void requireOpen() {
        if (!open) {
            throw new DeviceException(DeviceException.ILLEGAL_STATE, "FeliCa is not open");
        }
    }

    static void requireOfflineAccess() {
        requireOpen();
        if (onlineProcessing) {
            throw new DeviceException(DeviceException.ILLEGAL_STATE, "Online processing has already started");
        }
    }

    static UnsupportedOperationException unsupported(String operation) {
        return DoJaApiUnimplemented.unsupported(
                operation,
                "FeliCa card and reader/writer behavior remains unresolved on the desktop host"
        );
    }

    static byte[] idmFor(int card, int systemCode) {
        byte[] idm = new byte[8];
        idm[0] = (byte) 0x01;
        idm[1] = (byte) card;
        idm[2] = (byte) (systemCode >>> 8);
        idm[3] = (byte) systemCode;
        idm[4] = (byte) 0x5a;
        idm[5] = (byte) 0x51;
        idm[6] = (byte) (card ^ systemCode);
        idm[7] = (byte) (systemCode >>> 4);
        return idm;
    }

    static OfflineFelica offline(int card, int systemCode) {
        String key = card + ":" + systemCode;
        return OFFLINE.computeIfAbsent(key, ignored -> card == OfflineFelica.CARD_EXTERNAL
                ? new ThruRWOfflineFelica(card, systemCode, idmFor(card, systemCode))
                : new OfflineFelica(card, systemCode, idmFor(card, systemCode)));
    }

    static void resetRfState() {
        for (OfflineFelica offline : OFFLINE.values()) {
            if (offline instanceof ThruRWOfflineFelica thru) {
                thru.resetBaudRate();
            }
        }
    }
}
