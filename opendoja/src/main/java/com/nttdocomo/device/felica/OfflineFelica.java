package com.nttdocomo.device.felica;

import com.nttdocomo.lang.UnsupportedOperationException;

import java.io.IOException;

/**
 * Provides the means to access an internal card by offline processing.
 * Access to the free area is excluded.
 * Each instance of this class operates on the destination specified in
 * {@link Felica#getOfflineFelica(int, int)}.
 */
public class OfflineFelica {
    /** Constant value that represents an internal card (=0). */
    public static final int CARD_INTERNAL = 0;
    /** Constant value that represents an external card (=1). */
    public static final int CARD_EXTERNAL = 1;
    /** Constant value that represents a packet with a 2-byte node-code specification (=0x0000000000000000). */
    public static final long PARAM_NODE_CODE_LEN_2 = 0L;
    /** Constant value that represents a packet with a 4-byte node-code specification (=0x0000000000010000). */
    public static final long PARAM_NODE_CODE_LEN_4 = 0x00010000L;

    private final byte[] idm;
    private final byte[] cardVersion = new byte[]{0x05, 0x01};
    private final byte[] responseTimeInfo = new byte[0];
    private final int systemCode;
    private final int card;
    private int timeout;
    private int nodeCodeLength = 2;
    private long parameter = PARAM_NODE_CODE_LEN_2;

    OfflineFelica(int card, int systemCode, byte[] idm) {
        this.card = card;
        this.systemCode = systemCode;
        this.idm = idm.clone();
        this.timeout = card == CARD_EXTERNAL ? 250 : 500;
    }

    /**
     * Performs PIN-attribute reference.
     *
     * @param parameters the PIN-attribute reference parameters
     * @return never returns normally on the desktop host
     * @throws FelicaException if FeliCa processing fails
     * @throws IOException if low-level reader/writer access fails
     */
    public PINAttributeData[] checkPIN(CheckPINParameters parameters) throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        throw unsupported("com.nttdocomo.device.felica.OfflineFelica.checkPIN(com.nttdocomo.device.felica.CheckPINParameters)");
    }

    /**
     * Changes, enables or disables, or verifies a PIN.
     *
     * @param parameters the PIN parameters
     * @throws FelicaException if FeliCa processing fails
     * @throws IOException if low-level reader/writer access fails
     */
    public void executePIN(PINParameters parameters) throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        throw unsupported("com.nttdocomo.device.felica.OfflineFelica.executePIN(com.nttdocomo.device.felica.PINParameters)");
    }

    /**
     * Reads blocks.
     *
     * @param parameters the read parameters
     * @return never returns normally on the desktop host
     * @throws FelicaException if FeliCa processing fails
     * @throws IOException if low-level reader/writer access fails
     */
    public FelicaData[] read(ReadParameters parameters) throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        throw unsupported("com.nttdocomo.device.felica.OfflineFelica.read(com.nttdocomo.device.felica.ReadParameters)");
    }

    /**
     * Performs PIN verification, block reading, and reset consecutively.
     *
     * @param pinParameters the PIN parameters
     * @param readParameters the read parameters
     * @return never returns normally on the desktop host
     * @throws FelicaException if FeliCa processing fails
     * @throws IOException if low-level reader/writer access fails
     */
    public FelicaData[] read(InputPINParameters pinParameters, ReadParameters readParameters) throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        throw unsupported("com.nttdocomo.device.felica.OfflineFelica.read(com.nttdocomo.device.felica.InputPINParameters, com.nttdocomo.device.felica.ReadParameters)");
    }

    /**
     * Writes blocks.
     *
     * @param parameters the write parameters
     * @throws FelicaException if FeliCa processing fails
     * @throws IOException if low-level reader/writer access fails
     */
    public void write(WriteParameters parameters) throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        throw unsupported("com.nttdocomo.device.felica.OfflineFelica.write(com.nttdocomo.device.felica.WriteParameters)");
    }

    /**
     * Performs PIN verification, block writing, and reset consecutively.
     *
     * @param pinParameters the PIN parameters
     * @param writeParameters the write parameters
     * @throws FelicaException if FeliCa processing fails
     * @throws IOException if low-level reader/writer access fails
     */
    public void write(InputPINParameters pinParameters, WriteParameters writeParameters) throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        throw unsupported("com.nttdocomo.device.felica.OfflineFelica.write(com.nttdocomo.device.felica.InputPINParameters, com.nttdocomo.device.felica.WriteParameters)");
    }

    /**
     * Gets the key version.
     *
     * @param serviceCode the service code
     * @return never returns normally on the desktop host
     * @throws FelicaException if FeliCa processing fails
     * @throws IOException if low-level reader/writer access fails
     */
    public byte[] getKeyVersion(int serviceCode) throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        throw unsupported("com.nttdocomo.device.felica.OfflineFelica.getKeyVersion(int)");
    }

    /**
     * Gets container issue information.
     *
     * @return never returns normally on the desktop host
     * @throws FelicaException if FeliCa processing fails
     * @throws IOException if low-level reader/writer access fails
     */
    public byte[] getContainerIssueInfo() throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        throw unsupported("com.nttdocomo.device.felica.OfflineFelica.getContainerIssueInfo()");
    }

    /**
     * Sets the attributes used by command packets on the processing-target card.
     *
     * @param parameter the packet attribute to set
     * @throws FelicaException if FeliCa processing fails
     * @throws IOException if low-level reader/writer access fails
     * @throws IllegalArgumentException if {@code parameter} is not supported
     */
    public void setParameter(long parameter) throws FelicaException, IOException {
        FelicaSupport.requireOpen();
        if (parameter == PARAM_NODE_CODE_LEN_2) {
            this.parameter = parameter;
            return;
        }
        if (parameter == PARAM_NODE_CODE_LEN_4) {
            this.parameter = parameter;
            return;
        }
        throw new IllegalArgumentException("parameter");
    }

    /**
     * Returns the IDm held by this object as the access destination.
     *
     * @return the IDm
     */
    public byte[] getIDm() {
        return idm.clone();
    }

    /**
     * Returns the card version of the card held by this object as the access destination.
     *
     * @return the card version
     */
    public byte[] getCardVersion() {
        return cardVersion.clone();
    }

    /**
     * Returns data related to command-processing time for the card held by this object as the access destination.
     *
     * @return the response-time information
     */
    public byte[] getResponseTimeInfo() {
        return responseTimeInfo.clone();
    }

    /**
     * Gets the system code held by this object as the access destination.
     *
     * @return the system code
     */
    public int getSystemCode() {
        return systemCode;
    }

    /**
     * Gets the target card for this object.
     *
     * @return the target card
     */
    public int getCard() {
        return card;
    }

    /**
     * Gets the timeout value for card processing by this object.
     *
     * @return the timeout value
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout value for card processing by this object.
     *
     * @param timeout the timeout value to set
     * @throws IllegalArgumentException if {@code timeout} is negative
     */
    public void setTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout");
        }
        this.timeout = timeout;
    }

    /**
     * Gets the packet type used for card processing by this object.
     *
     * @return the node-code length used by this object
     */
    public int getNodeCodeLength() {
        return nodeCodeLength;
    }

    /**
     * Sets the packet type used for card processing by this object.
     *
     * @param nodeCodeLength the node-code length to use; {@code 2} or {@code 4}
     * @throws IllegalArgumentException if {@code nodeCodeLength} is neither {@code 2} nor {@code 4}
     */
    public void setNodeCodeLength(int nodeCodeLength) {
        if (nodeCodeLength != 2 && nodeCodeLength != 4) {
            throw new IllegalArgumentException("nodeCodeLength");
        }
        this.nodeCodeLength = nodeCodeLength;
    }

    UnsupportedOperationException unsupported(String method) {
        return FelicaSupport.unsupported(method);
    }
}
