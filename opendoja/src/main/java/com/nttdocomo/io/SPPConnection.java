package com.nttdocomo.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines a connection used for communication via Serial Port Profile (SPP).
 */
public interface SPPConnection extends BTConnection {
    InputStream openInputStream() throws IOException;

    OutputStream openOutputStream() throws IOException;
}
