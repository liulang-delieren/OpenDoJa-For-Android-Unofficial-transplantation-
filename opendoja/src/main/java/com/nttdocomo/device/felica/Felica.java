package com.nttdocomo.device.felica;

import opendoja.host.DoJaApiUnimplemented;

import java.io.IOException;

/**
 * Basic class that controls the FeliCa function.
 * This API classifies use of the FeliCa function into online access, offline access, free-area access,
 * access from an external reader/writer, and ad-hoc communication.
 * After calling {@link #open()}, applications obtain the corresponding access object with
 * {@link #getOnlineFelica()}, {@link #getOfflineFelica(int, int)}, {@link #getFreeArea()}, or
 * {@link #getAdhocDataTransfer()}.
 */
public final class Felica {
    private Felica() {
    }

    /**
     * Opens FeliCa.
     * Calling this method supplies power to the device (the FeliCa IC chip).
     * If it is already open, this method does nothing.
     *
     * @throws FelicaException if opening FeliCa fails
     */
    public static void open() throws FelicaException {
        FelicaSupport.open = true;
    }

    /**
     * Closes FeliCa.
     * Calling this method cuts power to the device (the FeliCa IC chip).
     * If FeliCa is already closed, or if FeliCa has not been opened, this method does nothing.
     *
     * @throws FelicaException if closing FeliCa fails
     */
    public static void close() throws FelicaException {
        FelicaSupport.open = false;
        FelicaSupport.onlineProcessing = false;
        FelicaSupport.resetRfState();
        FelicaSupport.FREE_AREA.setReset(false);
    }

    /**
     * Gets an {@link OnlineFelica} object for online processing.
     * Even if this method is called multiple times, the same instance is always returned.
     *
     * @return the {@code OnlineFelica} object
     */
    public static OnlineFelica getOnlineFelica() {
        FelicaSupport.requireOpen();
        return FelicaSupport.ONLINE;
    }

    /**
     * Gets an {@link OfflineFelica} object for offline processing.
     * Polling is always performed when this method is called, regardless of the argument combination.
     * If an already-known IDm is obtained, the existing instance for that card and system is returned.
     *
     * @param card the target card; {@link OfflineFelica#CARD_INTERNAL} or {@link OfflineFelica#CARD_EXTERNAL}
     * @param systemCode the system code
     * @return an {@code OfflineFelica} object for an internal card, or a
     *         {@link ThruRWOfflineFelica} object for an external card
     * @throws FelicaException if polling fails
     * @throws IOException if low-level reader/writer access fails
     */
    public static OfflineFelica getOfflineFelica(int card, int systemCode) throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        if (card != OfflineFelica.CARD_INTERNAL && card != OfflineFelica.CARD_EXTERNAL) {
            throw new IllegalArgumentException("card");
        }
        if (systemCode < 0 || systemCode > 0xFFFF) {
            throw new IllegalArgumentException("systemCode");
        }
        return FelicaSupport.offline(card, systemCode);
    }

    /**
     * Gets a {@link FreeArea} object for free-area access.
     *
     * @return the {@code FreeArea} object
     */
    public static FreeArea getFreeArea() {
        FelicaSupport.requireOpen();
        return FelicaSupport.FREE_AREA;
    }

    /**
     * Gets an {@link AdhocDataTransfer} object for ad-hoc communication.
     *
     * @return the {@code AdhocDataTransfer} object
     */
    public static AdhocDataTransfer getAdhocDataTransfer() {
        FelicaSupport.requireOpen();
        return FelicaSupport.ADHOC;
    }

    /**
     * Stops RF power output.
     *
     * @throws FelicaException if stopping RF power fails
     * @throws IOException if low-level reader/writer access fails
     */
    public static void turnOffRFPower() throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        FelicaSupport.resetRfState();
    }

    /**
     * Resets FeliCa.
     *
     * @throws FelicaException if the reset fails
     * @throws IOException if low-level reader/writer access fails
     */
    public static void reset() throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        FelicaSupport.resetRfState();
        FelicaSupport.FREE_AREA.setReset(true);
    }

    /**
     * Deprecated.
     * Beginning with DoJa-5.0 (903i), access from an external reader/writer is accepted without calling
     * this method, so the method is not supported.
     *
     * @throws com.nttdocomo.lang.UnsupportedOperationException because this deprecated DoJa-5.1 API is not supported
     */
    public static void activate() throws FelicaException, IOException {
        throw DoJaApiUnimplemented.unsupported(
                "com.nttdocomo.device.felica.Felica.activate()",
                "The DoJa 5.1 API documents this method as unsupported after DoJa-5.0"
        );
    }

    /**
     * Deprecated.
     * Beginning with DoJa-5.0 (903i), access from an external reader/writer is accepted without calling
     * {@link #activate()}, so this method is not supported.
     *
     * @throws com.nttdocomo.lang.UnsupportedOperationException because this deprecated DoJa-5.1 API is not supported
     */
    public static void inactivate() throws FelicaException, IOException {
        throw DoJaApiUnimplemented.unsupported(
                "com.nttdocomo.device.felica.Felica.inactivate()",
                "The DoJa 5.1 API documents this method as unsupported after DoJa-5.0"
        );
    }

    /**
     * Registers a listener that receives events when push notifications are received from an external
     * reader/writer.
     *
     * @param listener the listener to register, or {@code null} to clear the listener
     */
    public static void setFelicaPushListener(FelicaPushListener listener) {
        FelicaSupport.pushListener = listener;
    }

    /**
     * Gets the node list for nodes related to this application that are currently in the remote individual
     * area stop state.
     *
     * @return the locked-node list
     * @throws FelicaException if the node list cannot be obtained
     */
    public static int[] getLockedNodeList() throws FelicaException {
        FelicaSupport.requireOpen();
        return new int[0];
    }
}
