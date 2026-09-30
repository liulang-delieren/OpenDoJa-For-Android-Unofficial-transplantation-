package com.nttdocomo.io;

import java.io.IOException;

/**
 * Defines a DDP communication connection with the speech-recognition backend server.
 */
public interface DDPConnection extends HttpConnection {
    void setDDP();

    @Override
    void setRequestProperty(String key, String value) throws IOException;

    @Override
    void connect() throws IOException;
}
