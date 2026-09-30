package com.nttdocomo.io;

import java.io.IOException;

/**
 * Defines a connection used when communicating as an OBEX server.
 */
public interface ServerObexConnection extends ObexConnection {
    void accept() throws IOException;

    void receiveRequest() throws IOException;

    int getOperation();

    void sendResponse(int responseCode) throws IOException;
}
