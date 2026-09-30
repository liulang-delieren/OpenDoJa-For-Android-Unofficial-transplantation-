package com.nttdocomo.io;

import java.io.IOException;

/**
 * Defines a connection used when communicating as an OBEX server in continuous data transfer by ad hoc communication.
 */
public interface FelicaServerObexConnection extends ServerObexConnection {
    @Override
    void accept() throws IOException;

    @Override
    void receiveRequest() throws IOException;

    @Override
    int getOperation();

    @Override
    void sendResponse(int responseCode) throws IOException;

    @Override
    void close();

    @Override
    int getCommMode();
}
