package com.nttdocomo.io;

import java.io.IOException;

/**
 * Defines a connection used when communicating as an OBEX client in continuous data transfer by ad hoc communication.
 */
public interface FelicaClientObexConnection extends ClientObexConnection {
    @Override
    void connect() throws IOException;

    @Override
    void connect(int mode) throws IOException;

    @Override
    void setOperation(int operation);

    @Override
    void sendRequest() throws IOException;

    @Override
    int getResponseCode();

    @Override
    void close();

    @Override
    int getCommMode();
}
