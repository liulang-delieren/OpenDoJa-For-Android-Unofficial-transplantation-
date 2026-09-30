package com.nttdocomo.opt.ui.j3d2;

import opendoja.host.OpenDoJaLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Decoded j3d2 animation resource.
 */
public final class C3DActionTable {
    private static final boolean DEBUG_3D = opendoja.host.OpenDoJaLaunchArgs.getBoolean(opendoja.host.OpenDoJaLaunchArgs.DEBUG3D);
    private final C3DData.ActionTableData data;

    public C3DActionTable(byte[] source) {
        this.data = C3DData.parseActionTable(source);
        if (DEBUG_3D) {
            OpenDoJaLog.debug(C3DActionTable.class, () -> "C3DActionTable created actions=" + data.actions().length);
        }
    }

    public C3DActionTable(InputStream source) {
        this(readAllBytes(source));
    }

    public int getNumAction() {
        return data.actions().length;
    }

    public int getMaxTime(int action) {
        if (action < 0 || action >= data.actions().length) {
            throw new IllegalArgumentException("action");
        }
        return C3DData.floatSecondsToMillis(data.actions()[action].duration());
    }

    C3DData.ActionTableData data() {
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
            throw new IllegalArgumentException("Could not read C3DActionTable stream", exception);
        }
    }
}
