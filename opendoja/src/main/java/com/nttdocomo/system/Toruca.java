package com.nttdocomo.system;

import com.nttdocomo.ui.MediaResource;
import java.util.Date;
import java.util.Locale;

/**
 * Defines Toruca data.
 */
public class Toruca {
    /** Indicates the Toruca-card type ({@code "card"}). */
    public static final String TYPE_CARD = "card";

    /** Indicates the Toruca-snip type ({@code "snip"}). */
    public static final String TYPE_SNIP = "snip";

    private byte[] version = {0x01, 0x00};
    private String type = TYPE_SNIP;
    private String url;
    private String data1;
    private String data2;
    private String data3;
    private byte[] category;
    private byte[] body;
    String ipid;
    int colorId = -1;
    String sortId;
    int redistributionId = 4;
    Date expirationDate;
    private String moveProperty;

    /**
     * Creates a Toruca object.
     */
    public Toruca() {
    }

    /**
     * Creates a Toruca object from Toruca-formatted binary data.
     *
     * @param data the Toruca binary data
     */
    public Toruca(byte[] data) {
        Toruca parsed = _SystemSupport.parseToruca(data);
        this.version = parsed.version;
        this.type = parsed.type;
        this.url = parsed.url;
        this.data1 = parsed.data1;
        this.data2 = parsed.data2;
        this.data3 = parsed.data3;
        this.category = parsed.category;
        this.body = parsed.body;
        this.ipid = parsed.ipid;
        this.colorId = parsed.colorId;
        this.sortId = parsed.sortId;
        this.redistributionId = parsed.redistributionId;
        this.expirationDate = parsed.expirationDate;
        this.moveProperty = parsed.moveProperty;
    }

    /**
     * Sets the Toruca version.
     *
     * @param version the version bytes
     */
    public void setVersion(byte[] version) {
        _SystemSupport.validateTorucaVersion(version);
        this.version = new byte[]{version[0], version[1]};
    }

    /**
     * Gets the Toruca version.
     *
     * @return a copy of the version bytes
     */
    public byte[] getVersion() {
        return _SystemSupport.copyBytes(version);
    }

    /**
     * Sets the Toruca type.
     *
     * @param type the Toruca type
     */
    public void setType(String type) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        String normalized = type.toLowerCase(Locale.ROOT);
        if (!TYPE_CARD.equals(normalized) && !TYPE_SNIP.equals(normalized)) {
            throw new IllegalArgumentException("Unsupported Toruca type: " + type);
        }
        this.type = normalized;
    }

    /**
     * Gets the Toruca type.
     *
     * @return the Toruca type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the retrieval URL.
     *
     * @param url the retrieval URL, or {@code null}
     */
    public void setURL(String url) {
        _SystemSupport.validateTorucaUrl(url);
        this.url = url;
    }

    /**
     * Gets the retrieval URL.
     *
     * @return the retrieval URL, or {@code null}
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets data field 1.
     *
     * @param data1 the value, or {@code null}
     */
    public void setData1(String data1) {
        this.data1 = data1;
    }

    /**
     * Gets data field 1.
     *
     * @return the value, or {@code null}
     */
    public String getData1() {
        return data1;
    }

    /**
     * Sets data field 2.
     *
     * @param data2 the value, or {@code null}
     */
    public void setData2(String data2) {
        this.data2 = data2;
    }

    /**
     * Gets data field 2.
     *
     * @return the value, or {@code null}
     */
    public String getData2() {
        return data2;
    }

    /**
     * Sets data field 3.
     *
     * @param data3 the value, or {@code null}
     */
    public void setData3(String data3) {
        this.data3 = data3;
    }

    /**
     * Gets data field 3.
     *
     * @return the value, or {@code null}
     */
    public String getData3() {
        return data3;
    }

    /**
     * Sets the category code.
     *
     * @param code the category code, or {@code null}
     */
    public synchronized void setCategory(byte[] code) {
        this.category = _SystemSupport.validateTorucaCategory(code);
    }

    /**
     * Gets the category code.
     *
     * @return a copy of the category code, or {@code null}
     */
    public synchronized byte[] getCategory() {
        return _SystemSupport.copyBytes(category);
    }

    /**
     * Sets the Toruca body.
     *
     * @param body the body bytes, or {@code null}
     */
    public void setBody(byte[] body) {
        this.body = _SystemSupport.copyBytes(body);
    }

    /**
     * Gets the Toruca body.
     *
     * @return a copy of the body bytes, or {@code null}
     */
    public byte[] getBody() {
        return _SystemSupport.copyBytes(body);
    }

    /**
     * Gets the IP-ID.
     *
     * @return the IP-ID, or {@code null}
     */
    public String getIPID() {
        return ipid;
    }

    /**
     * Gets the color ID.
     *
     * @return the color ID, or {@code -1}
     */
    public int getColorID() {
        return colorId;
    }

    /**
     * Gets the sort ID.
     *
     * @return the sort ID, or {@code null}
     */
    public String getSortID() {
        return sortId;
    }

    /**
     * Gets the redistribution ID.
     *
     * @return the redistribution ID, or {@code -1}
     */
    public int getRedistributionID() {
        return redistributionId;
    }

    /**
     * Gets the expiration date.
     *
     * @return the expiration date, or {@code null}
     */
    public Date getExpirationDate() {
        return expirationDate == null ? null : new Date(expirationDate.getTime());
    }

    /**
     * Sets a Toruca property value.
     *
     * @param key the property key
     * @param value the property value, or {@code null} to clear it
     */
    public void setProperty(String key, String value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key");
        }
        if (!MediaResource.X_DCM_MOVE.equalsIgnoreCase(key)) {
            return;
        }
        moveProperty = value;
    }

    /**
     * Gets a Toruca property value.
     *
     * @param key the property key
     * @return the property value, or {@code null}
     */
    public String getProperty(String key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key");
        }
        return MediaResource.X_DCM_MOVE.equalsIgnoreCase(key) ? moveProperty : null;
    }
}
