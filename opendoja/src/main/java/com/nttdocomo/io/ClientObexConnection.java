package com.nttdocomo.io;

import java.io.IOException;

/**
 * Defines a connection used when communicating as an OBEX client.
 */
public interface ClientObexConnection extends ObexConnection {
    void connect() throws IOException;

    void connect(int mode) throws IOException;

    void setOperation(int operation);

    void sendRequest() throws IOException;

    int getResponseCode();
}
