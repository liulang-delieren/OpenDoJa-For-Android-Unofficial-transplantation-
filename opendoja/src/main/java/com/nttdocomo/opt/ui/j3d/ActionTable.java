package com.nttdocomo.opt.ui.j3d;

import opendoja.g3d.MascotActionTableData;
import opendoja.g3d.MascotLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a class that holds action data representing model motion.
 * By setting an action on a {@link Figure} object, its posture can be changed.
 * Continuously changing the posture can also make it appear animated.
 * Depending on the handset, this class may not be supported; in that case an
 * {@link UnsupportedOperationException} occurs when a method is called.
 *
 * <p>Introduced in DoJa-2.0.</p>
 */
public class ActionTable {
    private final MascotActionTableData handle;

    /**
     * Creates an action-table object from MTRA data.
     *
     * @param data the byte sequence representing the MTRA data
     * @throws NullPointerException if {@code data} is {@code null}
     * @throws RuntimeException if the MTRA data is invalid
     */
    public ActionTable(byte[] data) {
        try {
            this.handle = MascotLoader.loadActionTable(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an action-table object from MTRA data.
     *
     * @param inputStream the input-stream object for obtaining the MTRA data
     * @throws NullPointerException if {@code inputStream} is {@code null}
     * @throws IOException if an I/O error occurs while reading the MTRA data
     * @throws RuntimeException if the MTRA data is invalid
     */
    public ActionTable(InputStream inputStream) throws IOException {
        this.handle = MascotLoader.loadActionTable(inputStream);
    }

    ActionTable(MascotActionTableData handle) {
        this.handle = handle;
    }

    /**
     * Gets the number of actions defined in this action table.
     *
     * @return the number of actions
     */
    public int getNumAction() {
        return handle == null ? 0 : handle.numActions();
    }

    /**
     * Gets the maximum frame value of the specified action among the actions
     * defined in this action table.
     *
     * @param action the index of the action whose maximum frame value is
     *               obtained; it must be greater than or equal to {@code 0} and
     *               less than the value returned by {@link #getNumAction()}
     * @return the maximum frame value
     * @throws IllegalArgumentException if {@code action} is invalid
     */
    public int getMaxFrame(int action) {
        return handle.maxFrame(action);
    }

    MascotActionTableData handle() {
        return handle;
    }
}
