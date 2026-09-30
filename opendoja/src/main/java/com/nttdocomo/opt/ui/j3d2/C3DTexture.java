package com.nttdocomo.opt.ui.j3d2;

import opendoja.host.OpenDoJaLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Decoded j3d2 texture-set resource.
 */
public final class C3DTexture {
    private static final boolean DEBUG_3D = opendoja.host.OpenDoJaLaunchArgs.getBoolean(opendoja.host.OpenDoJaLaunchArgs.DEBUG3D);
    private final C3DData.TextureData data;

    public C3DTexture(byte[] source) {
        this.data = C3DData.parseTexture(source);
        if (DEBUG_3D) {
            OpenDoJaLog.debug(C3DTexture.class, () -> "C3DTexture created images=" + data.images().length);
        }
    }

    public C3DTexture(InputStream source) {
        this(readAllBytes(source));
    }

    C3DData.TextureData data() {
        return data;
    }

    public void dispose() {
    }

    private static byte[] readAllBytes(InputStream source) {
        if (source == null) {
            throw new NullPointerException("source");
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = source.read(buffer)) >= 0) {
                if (read > 0) {
                    out.write(buffer, 0, read);
                }
            }
            return out.toByteArray();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read C3DTexture stream", exception);
        }
    }
}
