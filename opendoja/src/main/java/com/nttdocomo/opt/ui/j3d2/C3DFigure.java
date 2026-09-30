package com.nttdocomo.opt.ui.j3d2;

import opendoja.host.OpenDoJaLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Decoded j3d2 figure resource.
 */
public final class C3DFigure {
    private static final boolean DEBUG_3D = opendoja.host.OpenDoJaLaunchArgs.getBoolean(opendoja.host.OpenDoJaLaunchArgs.DEBUG3D);
    private final C3DData.FigureData data;
    private C3DTexture texture;
    private C3DActionTable actionTable;
    private int action;
    private float timeSeconds;

    public C3DFigure(byte[] source) {
        this.data = C3DData.parseFigure(source);
        if (DEBUG_3D) {
            OpenDoJaLog.debug(C3DFigure.class, () -> "C3DFigure created nodes=" + data.nodes().size()
                    + " coordSets=" + data.coordSets().size());
        }
    }

    public C3DFigure(InputStream source) {
        this(readAllBytes(source));
    }

    public void setTexture(C3DTexture texture) {
        this.texture = texture;
        if (DEBUG_3D) {
            OpenDoJaLog.debug(C3DFigure.class, () -> "C3DFigure setTexture present=" + (texture != null));
        }
    }

    public void setPostureByTime(C3DActionTable actionTable, int action, int timeMillis) {
        this.actionTable = actionTable;
        this.action = action;
        this.timeSeconds = timeMillis / 1000f;
        if (DEBUG_3D) {
            OpenDoJaLog.debug(C3DFigure.class, () -> "C3DFigure setPosture action=" + action
                    + " timeMs=" + timeMillis
                    + " actionTablePresent=" + (actionTable != null));
        }
    }

    public int getAnimationTime() {
        return C3DData.floatSecondsToMillis(timeSeconds);
    }

    C3DData.FigureData data() {
        return data;
    }

    C3DTexture texture() {
        return texture;
    }

    C3DActionTable actionTable() {
        return actionTable;
    }

    int action() {
        return action;
    }

    float timeSeconds() {
        return timeSeconds;
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
            throw new IllegalArgumentException("Could not read C3DFigure stream", exception);
        }
    }
}
